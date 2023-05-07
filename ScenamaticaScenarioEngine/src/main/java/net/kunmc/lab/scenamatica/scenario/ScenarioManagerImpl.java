package net.kunmc.lab.scenamatica.scenario;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioAlreadyRunningException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotRunningException;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.MilestoneManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ScenarioManagerImpl implements ScenarioManager
{
    @Getter
    private final ScenamaticaRegistry registry;
    private final ActionManager actionManager;
    @Getter
    private final MilestoneManager milestoneManager;
    private final Multimap<Plugin, ScenarioEngine> engines;
    private final ScenarioQueue queue;
    @NotNull
    @Getter(AccessLevel.PACKAGE)
    private final TestReporter testReporter;
    private final TickListener tickListener;

    @Getter
    @Nullable
    private ScenarioEngine currentScenario;
    @Getter
    private boolean enabled;

    public ScenarioManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionManager = registry.getActionManager();
        this.testReporter = registry.getTestReporter();
        this.milestoneManager = new MilestoneManagerImpl();
        this.engines = ArrayListMultimap.create();
        this.tickListener = new TickListener(registry, this);
        this.queue = new ScenarioQueue(registry, this);
        this.currentScenario = null;
        this.enabled = true;
    }

    @Override
    public void init()
    {
        this.tickListener.init();
        this.queue.start();
    }

    @Override
    public boolean isRunning()
    {
        return this.currentScenario != null && this.currentScenario.isRunning();
    }

    @Override
    @NotNull
    public ScenarioResult startScenarioInterrupt(@NotNull Plugin plugin, @NotNull String scenarioName, boolean cancelRunning)
            throws ScenarioException
    {
        return this.startScenarioInterrupt(plugin, scenarioName, TriggerType.MANUAL_DISPATCH, cancelRunning);
    }

    @SneakyThrows(ScenarioNotRunningException.class)
    @Override
    @NotNull
    public ScenarioResult startScenarioInterrupt(@NotNull Plugin plugin, @NotNull String scenarioName, @NotNull TriggerType triggerType, boolean cancelRunning)
            throws ScenarioAlreadyRunningException, TriggerNotFoundException, ScenarioNotFoundException
    {
        if (!this.enabled)
            throw new IllegalStateException("Scenamatica is disabled.");
        else if (this.isRunning() && !cancelRunning)
            throw new ScenarioAlreadyRunningException("Another scenario is running.");

        AtomicReference<ScenarioResult> result = new AtomicReference<>();
        CyclicBarrier barrier = new CyclicBarrier(2);
        Consumer<ScenarioResult> callback = r -> {
            result.set(r);
            try
            {
                barrier.await();
            }
            catch (Exception e)
            {
                this.registry.getExceptionHandler().report(e);
            }
        };

        Pair<ScenarioEngine, TriggerBean> runInfo = this.getRunInfoOrThrow(plugin, scenarioName, triggerType);
        ScenarioEngine engine = runInfo.getLeft();
        TriggerBean trigger = runInfo.getRight();

        this.queue.addInterrupt(engine, trigger, callback);
        if (this.isRunning())  // cancelRunning == true
            this.cancel();

        try
        {
            barrier.await();
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
        }

        return result.get();
    }

    @Override
    public void queueScenario(@NotNull Plugin plugin, @NotNull String scenarioName, @NotNull TriggerType triggerType)
            throws ScenarioNotFoundException, TriggerNotFoundException
    {
        Pair<ScenarioEngine, TriggerBean> runInfo = this.getRunInfoOrThrow(plugin, scenarioName, triggerType);
        ScenarioEngine engine = runInfo.getLeft();
        TriggerBean trigger = runInfo.getRight();

        this.queueScenario(engine, trigger);
    }

    private void queueScenario(@NotNull ScenarioEngine engine, @NotNull TriggerBean trigger)
    {
        this.queue.add(engine, trigger, null);
    }

    @Override
    public void dequeueScenarios(@NotNull Plugin plugin)
    {
        this.queue.removeAll(plugin);
    }

    @Override
    public void dequeueScenario(@NotNull Plugin plugin, @NotNull String scenarioName)
    {
        this.queue.remove(plugin, scenarioName);
    }

    private Pair<ScenarioEngine, TriggerBean> getRunInfoOrThrow(Plugin plugin, String scenarioName, TriggerType trigger)
            throws ScenarioNotFoundException, TriggerNotFoundException
    {
        ScenarioEngine engine = this.engines.get(plugin).stream().parallel()
                .filter(e -> e.getScenario().getName().equals(scenarioName))
                .findFirst()
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioName));
        TriggerBean triggerBean = engine.getTriggerActions().stream().parallel()
                .map(CompiledTriggerAction::getTrigger)
                .filter(t -> t.getType() == trigger)
                .findFirst()
                .orElseThrow(() -> new TriggerNotFoundException(trigger));

        return Pair.of(engine, triggerBean);
    }

    /* non-public */ ScenarioResult runScenario(ScenarioEngine engine, TriggerBean trigger)
            throws TriggerNotFoundException
    {
        if (!engine.getPlugin().isEnabled())
            throw new IllegalStateException("Plugin is disabled.");
        else if (!this.enabled)
            throw new IllegalStateException("Scenamatica is disabled.");

        this.currentScenario = engine;
        ScenarioResult result = engine.start(trigger);
        this.currentScenario = null;
        this.testReporter.onTestEnd(engine, result);

        return result;
    }

    @Override
    public void cancel() throws ScenarioNotRunningException
    {
        if (this.currentScenario == null)
            throw new ScenarioNotRunningException();
        else if (!this.currentScenario.isRunning())
            throw new ScenarioNotRunningException(this.currentScenario.getScenario().getName());

        this.currentScenario.cancel();
    }

    @Override
    public void unloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.milestoneManager.revokeAllMilestones(plugin);
        Iterator<ScenarioEngine> iterator = this.engines.get(plugin).iterator();
        while (iterator.hasNext())
        {
            ScenarioEngine engine = iterator.next();
            if (engine.isRunning())
                engine.cancel();

            this.registry.getTriggerManager().unregisterTrigger(engine);

            iterator.remove();
        }

        this.registry.getScenarioFileManager().unloadPluginScenarios(plugin);
    }

    @Override
    public void loadPluginScenarios(@NotNull Plugin plugin)
    {
        if (!this.registry.getScenarioFileManager().reloadPluginScenarios(plugin))
            throw new IllegalStateException("Failed to reload plugin scenarios.");

        Map<String, ScenarioFileBean> scenarios = this.registry.getScenarioFileManager().getPluginScenarios(plugin);
        assert scenarios != null;

        List<ScenarioEngine> engines = new ArrayList<>(scenarios.size());
        scenarios.values().stream()
                .map(scenario -> new ScenarioEngineImpl(
                                this.registry,
                                this,
                                this.actionManager,
                                this.testReporter,
                                plugin,
                                scenario
                        )
                )
                .forEach(engines::add);

        this.engines.putAll(plugin, engines);
        this.runOnLoadScenarios(engines);
    }

    @Override
    public void reloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.unloadPluginScenarios(plugin);
        this.loadPluginScenarios(plugin);
    }

    @SneakyThrows(ScenarioNotRunningException.class)
    @Override
    public void shutdown()
    {
        if (this.isRunning())
            this.cancel();

        this.queue.shutdown();
        this.engines.clear();
    }

    @Override
    @SneakyThrows(ScenarioNotRunningException.class)
    public void setEnabled(boolean enabled)
    {
        if (this.enabled == enabled)
            return;
        else if (!enabled && this.isRunning())
            this.cancel();

        this.enabled = enabled;
    }

    private void runOnLoadScenarios(List<? extends ScenarioEngine> scenarios)
    {
        for (ScenarioEngine engine : scenarios)
        {
            for (CompiledTriggerAction action : engine.getTriggerActions())
            {
                if (action.getTrigger().getType() == TriggerType.ON_LOAD)
                {
                    this.queueScenario(engine, action.getTrigger());
                    break;
                }
            }
        }
    }
}
