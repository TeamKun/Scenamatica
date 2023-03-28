package net.kunmc.lab.scenamatica.scenario;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ScenarioManagerImpl implements ScenarioManager
{
    private final ScenamaticaRegistry registry;
    private final ActionManager actionManager;
    private final Multimap<Plugin, ScenarioEngine> engines;
    @Getter
    @Nullable
    private ScenarioEngine currentScenario;

    public ScenarioManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionManager = registry.getActionManager();
        this.engines = ArrayListMultimap.create();
    }

    @Override
    public boolean isRunning()
    {
        return this.currentScenario != null && this.currentScenario.isRunning();
    }

    @Override
    public TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName)
    {
        if (this.isRunning())
            throw new IllegalStateException("Scenario is already running.");

        ScenarioEngine engine = this.engines.get(plugin).stream().parallel()
                .filter(e -> e.getScenario().getName().equals(scenarioName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found."));
        TriggerBean manualDispatchTrigger = engine.getTriggerActions().stream().parallel()
                .map(CompiledTriggerAction::getTrigger)
                .filter(t -> t.getType() == TriggerType.MANUAL_DISPATCH)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The scenario cannot be started manually."));

        this.currentScenario = engine;
        TestResult result = engine.start(manualDispatchTrigger);
        this.currentScenario = null;

        return result;
    }

    @Override
    public void cancel()
    {
        if (this.currentScenario == null)
            throw new IllegalStateException("Scenario is not running.");
        else if (!this.currentScenario.isRunning())
            throw new IllegalStateException("Scenario is not running.");

        this.currentScenario.cancel();
    }

    @Override
    public void unloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.engines.removeAll(plugin);
        this.registry.getScenarioFileManager().unloadPluginScenarios(plugin);
    }

    @Override
    public void loadPluginScenarios(@NotNull Plugin plugin)
    {
        if (!this.registry.getScenarioFileManager().reloadPluginScenarios(plugin))
            throw new IllegalStateException("Failed to reload plugin scenarios.");

        Map<String, ScenarioFileBean> scenarios = this.registry.getScenarioFileManager().getPluginScenarios(plugin);
        assert scenarios != null;

        scenarios.values().stream()
                .map(scenario -> new ScenarioEngineImpl(this.registry, this.actionManager, plugin, scenario))
                .forEach(engine -> this.engines.put(plugin, engine));
    }

    @Override
    public void reloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.unloadPluginScenarios(plugin);
        this.loadPluginScenarios(plugin);
    }

    @Override
    public void shutdown()
    {
        if (this.isRunning())
            this.cancel();

        this.engines.clear();
    }
}
