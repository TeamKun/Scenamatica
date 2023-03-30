package net.kunmc.lab.scenamatica.scenario;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioAlreadyRunningException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotRunningException;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
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
    @NotNull
    private final TestReporter testReporter;
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
        this.engines = ArrayListMultimap.create();
        this.currentScenario = null;
        this.enabled = true;
    }

    @Override
    public boolean isRunning()
    {
        return this.currentScenario != null && this.currentScenario.isRunning();
    }

    @Override
    @NotNull
    public TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName)
            throws ScenarioException, ContextPreparationException
    {
        return this.startScenario(plugin, scenarioName, TriggerType.MANUAL_DISPATCH);
    }

    @Override
    @NotNull
    public TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName, @NotNull TriggerType triggerType)
            throws ScenarioException, ContextPreparationException
    {
        if (this.isRunning())
        {
            assert this.currentScenario != null;
            throw new ScenarioAlreadyRunningException(scenarioName, this.currentScenario.getScenario().getName());
        }
        else if (!this.enabled)
            throw new ScenarioException("Scenamatica is disabled.");

        ScenarioEngine engine = this.engines.get(plugin).stream().parallel()
                .filter(e -> e.getScenario().getName().equals(scenarioName))
                .findFirst()
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioName));
        TriggerBean trigger = engine.getTriggerActions().stream().parallel()
                .map(CompiledTriggerAction::getTrigger)
                .filter(t -> t.getType() == triggerType)
                .findFirst()
                .orElseThrow(() -> new TriggerNotFoundException(triggerType));

        this.testReporter.onTestStart(engine.getScenario(), trigger);
        this.currentScenario = engine;
        TestResult result = engine.start(trigger);
        this.currentScenario = null;
        this.testReporter.onTestEnd(engine.getScenario(), result);

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
                .map(scenario -> new ScenarioEngineImpl(this.registry, this.actionManager, this.testReporter, plugin, scenario))
                .forEach(engine -> this.engines.put(plugin, engine));
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
}
