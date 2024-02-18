package org.kunlab.scenamatica.action;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.server.log.ServerLogHandler;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.trigger.TriggerManager;

import java.util.ArrayDeque;
import java.util.Deque;

public class ActionRunManagerImpl implements ActionRunManager
{
    @Getter
    private final WatcherManager watcherManager;
    private final Deque<CompiledAction> actionQueue;
    private final ServerLogHandler serverLogHandler;

    private BukkitTask runner;

    public ActionRunManagerImpl(@NotNull TriggerManager triggerManager,
                                @NotNull ExceptionHandler exceptionHandler)
    {
        this.watcherManager = new WatcherManagerImpl(triggerManager, exceptionHandler);
        this.actionQueue = new ArrayDeque<>();
        this.serverLogHandler = new ServerLogHandler(Bukkit.getServer());

        this.runner = null;
    }

    @Override
    public void init(@NotNull Plugin scenamatica)
    {
        this.serverLogHandler.init();

        this.runner = Runner.runTimer(scenamatica, this::executeNext, 0, 1);
    }

    @Override
    public void queueExecute(@NotNull CompiledAction entry)
    {
        this.actionQueue.add(entry);
    }

    @Override
    public void queueWatch(@NotNull ScenarioEngine engine,
                           @NotNull ScenarioFileStructure scenario,
                           @NotNull CompiledAction action,
                           @NotNull WatchType watchType)
    {
        this.watcherManager.registerWatcher(engine, action, scenario, watchType);
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
            ActionContext ctxt = entry.getContext();
            executable.execute(ctxt);
            entry.getOnExecute().accept(ctxt.createResult(entry), ScenarioType.ACTION_EXECUTE);
        }
        catch (Throwable e)
        {
            entry.getErrorHandler().accept(entry, e);
        }
    }
}
