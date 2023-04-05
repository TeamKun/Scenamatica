package net.kunmc.lab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@AllArgsConstructor
        /* non-public */ class ScenarioQueue extends BukkitRunnable
{
    private final ScenamaticaRegistry registry;
    private final ScenarioManagerImpl manager;
    private final Deque<QueueEntry> scenarioRunQueue;

    private List<TestResult> sessionResults;
    private long sessionStartedAt;

    public ScenarioQueue(ScenamaticaRegistry registry, ScenarioManagerImpl manager)
    {
        this.registry = registry;
        this.manager = manager;
        this.scenarioRunQueue = new ArrayDeque<>();
        this.sessionResults = new ArrayList<>();
        this.sessionStartedAt = 0;
    }

    /* non-public */ void add(ScenarioEngine engine, TriggerBean trigger, Consumer<TestResult> callback)
    {
        this.scenarioRunQueue.add(new QueueEntry(engine, trigger, callback));
    }

    /* non-public */ void addInterrupt(ScenarioEngine engine, TriggerBean trigger, Consumer<TestResult> callback)
    {
        this.scenarioRunQueue.addFirst(new QueueEntry(engine, trigger, callback));
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
        this.runTaskTimerAsynchronously(this.registry.getPlugin(), 0L, 1L);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException
    {
        this.scenarioRunQueue.clear();
        super.cancel();
    }

    @Override
    public void run()
    {
        if (!this.manager.isEnabled() || this.scenarioRunQueue.isEmpty() || this.manager.isRunning())
            return;
        else if (this.sessionStartedAt == 0)
            this.startSession();

        QueueEntry entry = this.scenarioRunQueue.pop();
        try
        {
            TestResult result = this.manager.runScenario(entry.getEngine(), entry.getTrigger());
            this.sessionResults.add(result);

            if (entry.getCallback() != null)
                entry.getCallback().accept(result);
        }
        catch (TriggerNotFoundException e)  // マトモな使い方したら発生しないはず。
        {
            this.registry.getExceptionHandler().report(e);
        }

        if (this.scenarioRunQueue.isEmpty())
            this.endSession();
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
        Consumer<TestResult> callback;

    }
}
