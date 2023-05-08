package net.kunmc.lab.scenamatica.scenario;

import lombok.Data;
import net.kunmc.lab.scenamatica.interfaces.scenario.QueuedScenario;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Data
public class ScenarioSessionImpl implements ScenarioSession
{
    private final ScenarioManagerImpl manager;
    private final long queuedAt;
    private final List<QueuedScenario> scenarios;

    private ArrayDeque<QueuedScenario> queue;
    private long startedAt;
    private boolean running;
    private long finishedAt;

    public ScenarioSessionImpl(ScenarioManagerImpl manager, @NotNull List<QueuedScenario> scenarios)
    {
        this.manager = manager;
        this.queuedAt = System.currentTimeMillis();
        this.scenarios = scenarios;
    }

    @Override
    public void add(@NotNull ScenarioEngine engine, @NotNull TriggerBean trigger, @Nullable Consumer<? super ScenarioResult> callback)
    {
        this.scenarios.add(new QueuedScenarioImpl(this.manager, engine, trigger, callback));
    }

    @Override
    public void remove(Plugin plugin)
    {
        if (this.hasRan())
            throw new IllegalStateException("Cannot remove scenario after ran.");

        this.scenarios.removeIf(scenario -> scenario.getEngine().getPlugin().equals(plugin));
    }

    @Override
    public void remove(Plugin plugin, String name)
    {
        if (this.hasRan())
            throw new IllegalStateException("Cannot remove scenario after ran.");

        this.scenarios.removeIf(scenario ->
                scenario.getEngine().getPlugin().getName().equalsIgnoreCase(plugin.getName())
                        && scenario.getEngine().getScenario().getName().equalsIgnoreCase(name)
        );
    }

    private boolean hasRan()
    {
        return !(this.running || this.startedAt == 0 || this.finishedAt == 0);
    }

    @Override
    public List<QueuedScenario> getScenarios()
    {
        return Collections.unmodifiableList(new ArrayList<>(this.scenarios));
    }

    @Override
    public long getStartedAt()
    {
        if (!this.running)
            throw new IllegalStateException("Scenario is not running.");
        return this.startedAt;
    }

    @Override
    public long getFinishedAt()
    {
        if (this.running)
            throw new IllegalStateException("Scenario is still running.");
        return this.finishedAt;
    }

    /* non-public */ void onStart()
    {
        this.queue = new ArrayDeque<>(this.scenarios);
        this.startedAt = System.currentTimeMillis();
        this.running = true;
    }

    /* non-public */ void onFinished()
    {
        this.running = false;
        this.finishedAt = System.currentTimeMillis();
    }

    @Nullable
        /* non-public */ QueuedScenario getNext()
    {
        return this.queue.isEmpty() ? null: this.queue.pop();
    }
}
