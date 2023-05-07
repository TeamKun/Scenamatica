package net.kunmc.lab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@AllArgsConstructor
        /* non-public */ class ScenarioQueue
{
    private final ScenamaticaRegistry registry;
    private final ScenarioManagerImpl manager;
    private final Deque<QueueEntry> scenarioRunQueue;

    private List<ScenarioResult> sessionResults;
    private long sessionStartedAt;
    private SessionRunner runner;

    public ScenarioQueue(ScenamaticaRegistry registry, ScenarioManagerImpl manager)
    {
        this.registry = registry;
        this.manager = manager;
        this.scenarioRunQueue = new ArrayDeque<>();
        this.sessionResults = new ArrayList<>();
        this.sessionStartedAt = 0;
    }

    /* non-public */ void add(ScenarioEngine engine, TriggerBean trigger, Consumer<ScenarioResult> callback)
    {
        this.scenarioRunQueue.add(new QueueEntry(engine, trigger, callback));
        this.runner.resume();
    }

    /* non-public */ void addInterrupt(ScenarioEngine engine, TriggerBean trigger, Consumer<ScenarioResult> callback)
    {
        this.scenarioRunQueue.addFirst(new QueueEntry(engine, trigger, callback));
        this.runner.resume();
    }

    /* non-public */ void remove(Plugin plugin, String name)
    {
        this.scenarioRunQueue.removeIf(entry -> entry.getEngine().getPlugin().equals(plugin) &&
                entry.getEngine().getScenario().getName().equals(name));
    }

    /* non-public */ void removeAll(Plugin plugin)
    {
        this.scenarioRunQueue.removeIf(entry -> entry.getEngine().getPlugin().equals(plugin));
    }

    /* non-public */ void start()
    {
        if (this.runner != null && this.runner.isRunning())
            throw new IllegalStateException("ScenarioQueue is already running.");

        this.runner = new SessionRunner();
        this.runner.runTaskAsynchronously(this.registry.getPlugin());
    }

    public void shutdown() throws IllegalStateException
    {
        this.scenarioRunQueue.clear();
        if (this.runner.isRunning())
            this.runner.cancel();
    }

    private void startSession()
    {
        this.manager.getTestReporter().onTestSessionStart(
                this.scenarioRunQueue.stream()
                        .map(QueueEntry::getEngine)
                        .collect(Collectors.toList())
        );

        this.sessionResults.clear();
        this.sessionStartedAt = System.currentTimeMillis();
    }

    private void endSession()
    {
        this.manager.getTestReporter().onTestSessionEnd(this.sessionResults, this.sessionStartedAt);

        this.sessionResults.clear();
        this.sessionStartedAt = 0;
    }

    @Value
    /* non-public */ static class QueueEntry
    {
        ScenarioEngine engine;
        TriggerBean trigger;
        @Nullable
        Consumer<ScenarioResult> callback;

    }

    private class SessionRunner extends BukkitRunnable
    {
        @Getter
        private boolean running;
        private final Object lock = new Object();
        private boolean paused;

        public void resume()
        {
            synchronized (this.lock)
            {
                if (this.paused)
                    this.lock.notify();

                this.paused = false;
            }
        }

        @Override
        public void run()
        {
            this.running = true;

            try
            {
                while (ScenarioQueue.this.manager.isEnabled() && this.running)
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
            if (ScenarioQueue.this.scenarioRunQueue.isEmpty() || ScenarioQueue.this.manager.isRunning())
                try
                {
                    synchronized (this.lock)
                    {
                        this.paused = true;
                        this.lock.wait();
                        return true;
                    }
                }
                catch (InterruptedException ignored)
                {
                    return false;
                }
            else if (ScenarioQueue.this.sessionStartedAt == 0)  // 0 ならまだセッションが開始されていない。
                ScenarioQueue.this.startSession();

            QueueEntry entry = ScenarioQueue.this.scenarioRunQueue.pop();

            ScenarioResult result = ScenarioQueue.this.manager.runScenario(entry.getEngine(), entry.getTrigger());
            ScenarioQueue.this.sessionResults.add(result);

            if (entry.getCallback() != null)
                entry.getCallback().accept(result);


            if (ScenarioQueue.this.scenarioRunQueue.isEmpty())
                ScenarioQueue.this.endSession();

            return true;
        }

        @Override
        public synchronized void cancel() throws IllegalStateException
        {
            this.running = false;
            super.cancel();
        }
    }
}
