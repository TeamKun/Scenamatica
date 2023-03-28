package net.kunmc.lab.scenamatica.action;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionCompiler;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.WatcherManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.apache.logging.log4j.util.BiConsumer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class ActionManagerImpl implements ActionManager
{
    private final ScenamaticaRegistry registry;
    @Getter
    private final ActionCompiler compiler;
    @Getter
    private final WatcherManager watcherManager;
    private final Deque<CompiledAction<?>> actionQueue;

    private BukkitTask runner;

    public ActionManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.compiler = new ActionCompilerImpl();
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

            CompiledAction<?> entry = this.actionQueue.pop();
            entry.execute();
        }, 0, 1);
    }

    @Override
    public <A extends ActionArgument> CompiledAction<A> queueExecute(@NotNull Action<A> action,
                                                                     @Nullable A argument,
                                                                     @Nullable BiConsumer<CompiledAction<A>, Throwable> onEexception,
                                                                     @Nullable Consumer<CompiledAction<A>> onSuccess)
    {
        CompiledAction<A> entry = new CompiledActionImpl<>(action, argument, onEexception, onSuccess);
        this.actionQueue.add(entry);
        return entry;
    }

    @Override
    public <A extends ActionArgument> void queueWatch(@NotNull Plugin plugin,
                                                      @NotNull ScenarioEngine engine,
                                                      @NotNull ScenarioFileBean scenario,
                                                      @NotNull Action<A> action,
                                                      @NotNull WatchType watchType,
                                                      @Nullable A argument)
    {
        this.watcherManager.registerWatcher(engine, action, argument, scenario, plugin, watchType);
    }
    @Override
    public void shutdown()
    {
        this.actionQueue.clear();
        if (this.runner != null)
            this.runner.cancel();
    }
}
