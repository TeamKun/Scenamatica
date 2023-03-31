package net.kunmc.lab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

@AllArgsConstructor
        /* non-public */ class ScenarioQueue extends BukkitRunnable
{
    private final ScenamaticaRegistry registry;
    private final ScenarioManagerImpl manager;
    private final Deque<QueueEntry> scenarioRunQueue;

    public ScenarioQueue(ScenamaticaRegistry registry, ScenarioManagerImpl manager)
    {
        this.registry = registry;
        this.manager = manager;
        this.scenarioRunQueue = new ArrayDeque<>();
    }

    /* non-public */ void add(ScenarioEngine engine, TriggerBean trigger, Consumer<TestResult> callback)
    {
        this.scenarioRunQueue.add(new QueueEntry(engine, trigger, callback));
    }

    /* non-public */ void addInterrupt(ScenarioEngine engine, TriggerBean trigger, Consumer<TestResult> callback)
    {
        this.scenarioRunQueue.addFirst(new QueueEntry(engine, trigger, callback));
    }

    /* non-public */ void start()
    {
        this.runTaskTimerAsynchronously(this.registry.getPlugin(), 0L, 1L);
    }

    @Override
    public void run()
    {
        if (!this.manager.isEnabled() || this.scenarioRunQueue.isEmpty())
            return;


        QueueEntry entry = this.scenarioRunQueue.pop();
        try
        {
            TestResult result = this.manager.runScenario(entry.getEngine(), entry.getTrigger());
            if (entry.getCallback() != null)
                entry.getCallback().accept(result);
        }
        catch (ScenarioException e)
        {
            this.registry.getExceptionHandler().report(e);
        }
        catch (ContextPreparationException e)
        {
            this.registry.getExceptionHandler().report(e);
        }
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
