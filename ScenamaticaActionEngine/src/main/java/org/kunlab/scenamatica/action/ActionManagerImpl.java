package org.kunlab.scenamatica.action;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.server.log.ServerLogHandler;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.ActionManager;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;

import java.util.ArrayDeque;
import java.util.Deque;

public class ActionManagerImpl implements ActionManager
{
    private final ScenamaticaRegistry registry;
    @Getter
    private final ActionCompiler compiler;
    @Getter
    private final WatcherManager watcherManager;
    private final Deque<CompiledAction<?>> actionQueue;
    private final ServerLogHandler serverLogHandler;

    private BukkitTask runner;

    public ActionManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.compiler = new ActionCompilerImpl();
        this.watcherManager = new WatcherManagerImpl(registry);
        this.actionQueue = new ArrayDeque<>();
        this.serverLogHandler = new ServerLogHandler(registry.getPlugin().getServer());

        this.runner = null;
    }

    @Override
    public void init()
    {
        this.serverLogHandler.init();

        this.runner = Runner.runTimer(this.registry.getPlugin(), () -> {
            if (this.actionQueue.isEmpty())
                return;

            CompiledAction<?> entry = this.actionQueue.pop();
            entry.execute();
        }, 0, 1);
    }

    @Override
    public <A extends ActionArgument> void queueExecute(@NotNull CompiledAction<A> entry)
    {
        this.actionQueue.add(entry);
    }

    @Override
    public <A extends ActionArgument> void queueWatch(@NotNull Plugin plugin,
                                                      @NotNull ScenarioEngine engine,
                                                      @NotNull ScenarioFileBean scenario,
                                                      @NotNull CompiledAction<A> action,
                                                      @NotNull WatchType watchType)
    {
        this.watcherManager.registerWatcher(engine, action, scenario, plugin, watchType);
    }

    @Override
    public void shutdown()
    {
        this.serverLogHandler.shutdown();
        this.actionQueue.clear();
        if (this.runner != null)
            this.runner.cancel();
    }
}
