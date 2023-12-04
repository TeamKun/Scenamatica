package org.kunlab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.SessionCreator;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.function.Consumer;

@AllArgsConstructor
        /* non-public */
class ScenarioQueue
{
    private final ScenamaticaRegistry registry;
    private final ScenarioManagerImpl manager;
    private final Deque<ScenarioSessionImpl> scenarioQueue;

    private ScenarioSessionImpl current;
    private SessionRunner runner;

    public ScenarioQueue(ScenamaticaRegistry registry, ScenarioManagerImpl manager)
    {
        this.registry = registry;
        this.manager = manager;
        this.scenarioQueue = new ArrayDeque<>();
    }

    /* non-public */ void add(ScenarioEngine engine, TriggerStructure trigger,
                              Consumer<? super ScenarioResult> callback, int maxAttemptCount)
    {
        this.scenarioQueue.add(new ScenarioSessionImpl(this.manager, Collections.singletonList(
                new QueuedScenarioImpl(this.manager, engine, trigger, callback, maxAttemptCount)
        )));
        this.runner.resume();
    }

    /* non-public */ void addAll(SessionCreator creator) throws TriggerNotFoundException, ScenarioNotFoundException
    {
        ScenarioSessionImpl session = new ScenarioSessionImpl(this.manager);
        for (SessionCreator.SessionElement elm : creator.getSessions())
        {
            ScenarioEngine engine = elm.getEngine();
            TriggerStructure trigger = null;
            if (engine == null)
            {
                Pair<ScenarioEngine, TriggerStructure> info =
                        this.manager.getRunInfoOrThrow(elm.getPlugin(), elm.getName(), elm.getType());
                engine = info.getLeft();
                trigger = info.getRight();
            }

            if (trigger == null)
                trigger = this.manager.getTriggerOrNull(engine, elm.getType());
            if (trigger == null)
                continue;  // addAll では, トリガーが見つからなかったら無視する。

            session.add(engine, trigger, elm.getCallback(), elm.getMaxAttempt());
        }

        this.scenarioQueue.add(session);
        this.runner.resume();
    }

    /* non-public */ void addInterrupt(ScenarioEngine engine, TriggerStructure trigger, Consumer<? super ScenarioResult> callback)
    {
        if (this.current != null)  // バックアップを取っておく。
            this.scenarioQueue.add(this.current);

        this.current = new ScenarioSessionImpl(this.manager, new ArrayList<>());

        this.current.add(engine, trigger, callback);

        this.runner.resume();
    }

    /* non-public */ void remove(Plugin plugin, String name)
    {
        if (this.current != null)
            this.current.remove(plugin, name);

        this.scenarioQueue.forEach(session -> session.remove(plugin, name));

        // ↑の操作で session が空になったら削除する
        this.scenarioQueue.removeIf(session -> session.getScenarios().isEmpty());
    }

    /* non-public */ void removeAll(Plugin plugin)
    {
        if (this.current != null)
            this.current.remove(plugin);

        this.scenarioQueue.forEach(session -> session.remove(plugin));

        // ↑の操作で session が空になったら削除する
        this.scenarioQueue.removeIf(session -> session.getScenarios().isEmpty());
    }

    /* non-public */ void start()
    {
        if (this.runner != null && this.runner.isRunning())
            throw new IllegalStateException("ScenarioQueue is already running.");

        this.runner = new SessionRunner(this.manager);
        this.runner.runTaskAsynchronously(this.registry.getPlugin());
    }

    public void shutdown() throws IllegalStateException
    {
        this.scenarioQueue.clear();
        if (this.runner != null && this.runner.isRunning())
            this.runner.cancel();

        this.runner = null;
    }

    @RequiredArgsConstructor
    private class SessionRunner extends BukkitRunnable
    {
        private final ScenarioManagerImpl manager;
        private final Object lock = new Object();
        @Getter
        private boolean running;
        private boolean paused;

        @Override
        public void run()
        {
            this.running = true;

            try
            {
                while (this.manager.isEnabled() && this.running)
                    if (!this.runOne())
                        return;
            }
            catch (Exception e)
            {
                ScenarioQueue.this.registry.getExceptionHandler().report(e);
            }

            this.running = false;
        }

        @SneakyThrows(TriggerNotFoundException.class)
        private boolean runOne()
        {
            if (ScenarioQueue.this.current == null && !this.pickNextSession())
                return this.pause();  // 次のセッションがない場合は待機。

            QueuedScenario next;
            if ((next = ScenarioQueue.this.current.getNext()) == null)
            {
                this.endSession();
                return true;
            }

            if (next.getAttemptCount() > 1)
                this.notifyRetryStart(next);

            ScenarioResult result = next.run();  // onStart() => run => onFinished => callback 処理までやる。

            if (result.getScenarioResultCause().isFailure() && next.getMaxAttemptCount() > 1)
                this.tryQueueRetry(next);

            return true;
        }

        private void notifyRetryStart(QueuedScenario scenario)
        {
            ScenarioQueue.this.registry.getLogger().info(
                    LangProvider.get(
                            "scenario.run.retry.start",
                            MsgArgs.of("scenarioName", scenario.getEngine().getScenario().getName())
                                    .add("count", scenario.getAttemptCount())
                                    .add("maxCount", scenario.getMaxAttemptCount())
                    ));
        }

        private void tryQueueRetry(QueuedScenario scenario)
        {
            int maxAttemptCount = scenario.getMaxAttemptCount();
            int retryCount = scenario.getAttemptCount();
            if (retryCount >= maxAttemptCount)
            {
                ScenarioQueue.this.registry.getLogger().warning(
                        LangProvider.get(
                                "scenario.run.retry.queue.failed",
                                MsgArgs.of("scenarioName", scenario.getEngine().getScenario().getName())
                                        .add("count", retryCount)
                                        .add("maxCount", maxAttemptCount)
                        ));
                return;
            }

            ScenarioQueue.this.registry.getLogger().info(
                    LangProvider.get(
                            "scenario.run.retry.queue.queued",
                            MsgArgs.of("scenarioName", scenario.getEngine().getScenario().getName())
                                    .add("count", retryCount)
                                    .add("maxCount", maxAttemptCount)
                    ));

            scenario.resetForRetry();
            ScenarioQueue.this.current.add(scenario);
        }

        @Override
        public synchronized void cancel() throws IllegalStateException
        {
            this.running = false;
            super.cancel();
        }

        private boolean pickNextSession()
        {
            if (ScenarioQueue.this.scenarioQueue.isEmpty())
                return false;

            ScenarioSessionImpl session = ScenarioQueue.this.scenarioQueue.pop();
            session.onStart();
            this.manager.getTestReporter().onTestSessionStart(session);

            ScenarioQueue.this.current = session;

            return true;
        }

        private void endSession()
        {
            ScenarioQueue.this.current.onFinished();
            this.manager.getTestReporter().onTestSessionEnd(ScenarioQueue.this.current);

            ScenarioQueue.this.current = null;
        }

        private boolean pause()
        {
            try
            {
                synchronized (this.lock)
                {
                    this.paused = true;
                    this.lock.wait();
                    return true; // 実行を再開する。
                }
            }
            catch (InterruptedException ignored)
            {
                return false; // 実行を終了する。
            }
        }

        public void resume()
        {
            synchronized (this.lock)
            {
                if (this.paused)
                    this.lock.notify();

                this.paused = false;
            }
        }
    }
}
