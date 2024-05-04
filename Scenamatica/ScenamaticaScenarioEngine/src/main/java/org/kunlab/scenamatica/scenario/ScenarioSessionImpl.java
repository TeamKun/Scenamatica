package org.kunlab.scenamatica.scenario;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioOrder;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenario.storages.SessionStorageImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Data
@Setter(AccessLevel.NONE)
public class ScenarioSessionImpl implements ScenarioSession
{
    private final ScenarioManagerImpl manager;
    private final long createdAt;
    private final List<QueuedScenario> scenarios;
    private final SessionStorage variables;

    private ArrayDeque<QueuedScenario> queue;
    private long startedAt;
    private boolean running;
    private long finishedAt;

    private QueuedScenario current;

    public ScenarioSessionImpl(ScenarioManagerImpl manager, @NotNull List<QueuedScenario> scenarios)
    {
        this.manager = manager;
        this.createdAt = System.currentTimeMillis();
        this.scenarios = new LinkedList<>(scenarios);  // 内部で変更するため, 不変リストとかを入れないように。
        this.variables = new SessionStorageImpl(manager.getRegistry(), this);
        this.sort();
    }

    public ScenarioSessionImpl(ScenarioManagerImpl manager)
    {
        this(manager, new ArrayList<>());
    }

    private void sort()
    {
        this.scenarios.sort((a, b) -> {
            int orderA = a.getEngine().getScenario().getOrder();
            int orderB = b.getEngine().getScenario().getOrder();
            if (orderA == ScenarioOrder.NORMAL.getOrder() && orderB == ScenarioOrder.NORMAL.getOrder())
            {  // 両方ともデフォルト値の場合はシナリオ名でソート。
                String nameA = a.getEngine().getScenario().getName();
                String nameB = b.getEngine().getScenario().getName();

                return nameA.compareTo(nameB);
            }

            return Integer.compare(orderA, orderB);
        });
    }

    @Override
    public void add(@NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger, @Nullable Consumer<? super ScenarioResult> callback, int maxAttemptCount)
    {
        this.add(new QueuedScenarioImpl(this.manager, engine, trigger, callback, maxAttemptCount));
        this.sort();
    }

    /* non-public */ void add(@NotNull QueuedScenario scenario)
    {
        this.scenarios.add(scenario);
        if (this.running)
            this.queue.add(scenario);
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
        if (!this.hasRan())
            throw new IllegalStateException("This scenario has not started yet.");
        return this.startedAt;
    }

    private void notifyRetryStart(QueuedScenario scenario)
    {
        this.manager.getRegistry().getLogger().info(
                LangProvider.get(
                        "scenario.run.retry.start",
                        MsgArgs.of("scenarioName", scenario.getEngine().getScenario().getName())
                                .add("count", scenario.getAttemptCount())
                                .add("maxCount", scenario.getMaxAttemptCount())
                ));
    }

    @Override
    public ScenarioResult runNext() throws TriggerNotFoundException
    {
        if (!this.hasNext())
            throw new IllegalStateException("There is no next scenario.");

        QueuedScenario next = this.queue.pop();

        if (next.getAttemptCount() > 1)
            this.notifyRetryStart(next);

        this.current = next;
        try
        {
            return next.run(this.variables);  // onStart() => run => onFinished => callback 処理までやる。
        }
        finally
        {
            this.current = null;
        }
    }

    @Override
    public long getFinishedAt()
    {
        if (!this.hasRan())
            throw new IllegalStateException("This scenario has not finished yet.");
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
        for (QueuedScenario scenario : this.scenarios)
            scenario.getEngine().releaseScenarioInputs();

        this.variables.clear();
    }

    public boolean hasNext()
    {
        return !(this.queue == null || this.queue.isEmpty());
    }

    @NotNull
        /* non-public */ QueuedScenario pollNext()
    {
        if (!this.hasNext())
            throw new IllegalStateException("There is no next scenario.");
        return this.queue.getFirst();
    }
}
