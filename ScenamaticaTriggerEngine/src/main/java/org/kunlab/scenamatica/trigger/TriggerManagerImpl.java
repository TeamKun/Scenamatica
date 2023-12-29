package org.kunlab.scenamatica.trigger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.SessionCreator;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;
import org.kunlab.scenamatica.interfaces.trigger.TriggerManager;
import org.kunlab.scenamatica.trigger.arguments.ActionTriggerArgument;

import java.util.List;
import java.util.Locale;

public class TriggerManagerImpl implements TriggerManager
{
    static
    {
        TriggerType.ON_ACTION.setArgumentType(ActionTriggerArgument.class);
    }

    private final ScenamaticaRegistry registry;
    private final ActionRunManager actionManager;
    private final Multimap<ScenarioEngine, TriggerStructure> triggers;  // シナリオ名 / トリガー

    private final List<TriggerType> ignoreTypes;
    private final int defaultMaxAttempts;

    public TriggerManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionManager = registry.getActionManager();
        this.triggers = ArrayListMultimap.create();
        this.ignoreTypes = registry.getEnvironment().getIgnoreTriggerTypes();
        this.defaultMaxAttempts = registry.getEnvironment().getMaxAttemptCount();
    }

    @Override
    public void bakeTriggers(@NotNull ScenarioEngine engine)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        String scenarioName = scenario.getName().toLowerCase(Locale.ROOT);
        if (this.triggers.containsKey(engine))
            throw new IllegalArgumentException("The engine is already baked: " + scenarioName);

        this.triggers.putAll(engine, scenario.getTriggers());

        // ON_ACTION なトリガを監視対象に。
        scenario.getTriggers().forEach(t -> {
            TriggerType type = t.getType();
            if (type == TriggerType.ON_ACTION)
                this.registerActionTrigger(engine, t);
        });
    }

    @Override
    public void performTriggerFire(@NotNull Plugin plugin,
                                   @NotNull String scenarioName,
                                   @NotNull TriggerType type,
                                   @Nullable TriggerArgument argument) throws ScenarioException
    {
        if (this.shouldIgnore(type, argument))
            return;

        ScenarioEngine engine = this.registry.getScenarioManager().getEngine(plugin, scenarioName);

        if (engine == null)
            throw new ScenarioException(scenarioName, "Scenario " + scenarioName + " is not loaded.");
        else if (!this.triggers.containsKey(engine))
            throw new ScenarioException(scenarioName, "Scenario " + scenarioName + " is not baked.");

        SessionCreator creator = this.registry.getScenarioManager().newSession();
        engine.getScenario().getTriggers().stream()
                .filter(t -> t.getType() == type)
                .forEach(t -> creator.add(engine, t.getType(), this.defaultMaxAttempts));

        this.registry.getScenarioManager().queueScenario(creator);
    }

    /**
     * トリガを実行します。
     *
     * @param type トリガタイプ
     */
    @Override
    public void performTriggerFire(@NotNull TriggerType type)
    {
        this.performTriggerFire(this.registry.getScenarioManager().getEngines(), type);
    }

    @Override
    @SneakyThrows(ScenarioException.class)
    public void performTriggerFire(@NotNull List<? extends ScenarioEngine> engines, @NotNull TriggerType type)
    {
        if (this.shouldIgnore(type, null))
            return;

        SessionCreator creator = this.registry.getScenarioManager().newSession();
        for (ScenarioEngine engine : engines)
            creator.add(engine, type, this.defaultMaxAttempts);

        if (creator.isEmpty())
            return;

        this.registry.getScenarioManager().queueScenario(creator);
    }

    private boolean shouldIgnore(@NotNull TriggerType type, @Nullable TriggerArgument argument)
    {
        if (this.ignoreTypes.contains(type))
            return true;

        return type.getArgumentType() != null && argument == null;  // 引数が必要なのにない場合は無視。
    }

    private void registerActionTrigger(ScenarioEngine engine, TriggerStructure actionTrigger)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        TriggerArgument triggerArgument = actionTrigger.getArgument();
        if (!(triggerArgument instanceof ActionStructure))
            throw new IllegalArgumentException("Action trigger argument required");

        ActionStructure argument = (ActionStructure) triggerArgument;

        CompiledAction action = this.actionManager.getCompiler().compile(
                engine,
                ScenarioType.ACTION_EXPECT,
                argument,
                null,  /* reportErrorTo */
                null  /* onSuccess */
        );

        // 型はコンパイラでチェック済みなのでキャストしておく。
        this.actionManager.queueWatch(
                engine,
                scenario,
                action,
                WatchType.TRIGGER
        );
    }

    @Override
    public void unregisterTrigger(@NotNull ScenarioEngine engine)
    {
        this.triggers.removeAll(engine);
    }
}
