package net.kunmc.lab.scenamatica.action;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.action.WatcherManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class ActionManagerImpl implements ActionManager
{
    private final ScenamaticaRegistry registry;
    @Getter
    private final WatcherManager watcherManager;
    private final Deque<ActionQueueEntry<?>> actionQueue;

    private BukkitTask runner;

    public ActionManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.watcherManager = new WatcherManagerImpl(registry);
        this.actionQueue = new ArrayDeque<>();

        this.runner = null;
    }

    @Override
    public void init()
    {
        this.runner = Runner.runTimer(this.registry.getPlugin(), () -> {
            if (this.actionQueue.isEmpty())
                return;

            ActionQueueEntry<?> entry = this.actionQueue.pop();
            entry.execute();
        }, 0, 1);
    }

    @Override
    public <A extends ActionArgument> void queueExecute(@NotNull Action<A> action, @Nullable A argument)
    {
        this.actionQueue.add(new ActionQueueEntry<>(action, argument));
    }

    @Override
    public <A extends ActionArgument> void queueWatch(@NotNull Plugin plugin,
                                                      @NotNull ScenarioFileBean scenario,
                                                      @NotNull Action<A> action,
                                                      @NotNull WatchType watchType,
                                                      @Nullable A argument)
    {
        this.watcherManager.registerWatcher(action, argument, scenario, plugin, watchType);
    }

    @Override
    public void startScenario(@NotNull ScenarioFileBean scenario)
    {
        for (ScenarioBean oneScenario : scenario.getScenario())
        {
            ActionQueueEntry<?> entry = ActionCompiler.compile(this.registry, oneScenario.getAction());
            this.actionQueue.add(entry);
        }
    }

    @Override
    public void shutdown()
    {
        this.actionQueue.clear();
        if (this.runner != null)
            this.runner.cancel();
    }
}
