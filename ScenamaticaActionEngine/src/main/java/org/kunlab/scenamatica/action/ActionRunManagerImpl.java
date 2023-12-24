package org.kunlab.scenamatica.action;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.server.log.ServerLogHandler;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.ArrayDeque;
import java.util.Deque;

public class ActionRunManagerImpl implements ActionRunManager
{
    private final ScenamaticaRegistry registry;
    @Getter
    private final ActionCompiler compiler;
    @Getter
    private final WatcherManager watcherManager;
    private final Deque<CompiledAction> actionQueue;
    private final ServerLogHandler serverLogHandler;

    private BukkitTask runner;

    public ActionRunManagerImpl(@NotNull ScenamaticaRegistry registry)
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

        this.runner = Runner.runTimer(this.registry.getPlugin(), this::executeNext, 0, 1);
    }

    @Override
    public void queueExecute(@NotNull CompiledAction entry)
    {
        this.actionQueue.add(entry);
    }

    @Override
    public void queueWatch(@NotNull Plugin plugin,
                           @NotNull ScenarioEngine engine,
                           @NotNull ScenarioFileStructure scenario,
                           @NotNull CompiledAction action,
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

    private void executeNext()
    {
        if (this.actionQueue.isEmpty())
            return;

        CompiledAction entry = this.actionQueue.pop();
        try
        {
            assert entry.getExecutor() instanceof Executable;

            Executable executable = (Executable) entry.getExecutor();
            executable.execute(entry.getEngine(), entry.getArgument());
            entry.getOnExecute().accept(entry);
        }
        catch (Throwable e)
        {
            entry.getErrorHandler().accept(entry, e);
        }
    }
}
