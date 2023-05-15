package net.kunmc.lab.scenamatica.trigger;

import com.google.common.collect.*;
import net.kunmc.lab.scenamatica.enums.*;
import net.kunmc.lab.scenamatica.exceptions.scenario.*;
import net.kunmc.lab.scenamatica.interfaces.*;
import net.kunmc.lab.scenamatica.interfaces.action.*;
import net.kunmc.lab.scenamatica.interfaces.scenario.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.*;
import net.kunmc.lab.scenamatica.interfaces.trigger.*;
import net.kunmc.lab.scenamatica.trigger.arguments.*;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class TriggerManagerImpl implements TriggerManager
{
    static
    {
        TriggerType.ON_ACTION.setArgumentType(ActionTriggerArgument.class);
    }

    private final ScenamaticaRegistry registry;
    private final ActionManager actionManager;
    private final Multimap<ScenarioEngine, TriggerBean> triggers;  // シナリオ名 / トリガー

    public TriggerManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionManager = registry.getActionManager();
        this.triggers = ArrayListMultimap.create();
    }

    @Override
    public void bakeTriggers(@NotNull ScenarioEngine engine)
    {
        ScenarioFileBean scenario = engine.getScenario();

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
        ScenarioEngine engine = this.registry.getScenarioManager().getEngine(plugin, scenarioName);

        if (engine == null)
            throw new ScenarioException(scenarioName, "Scenario " + scenarioName + " is not loaded.");
        else if (!this.triggers.containsKey(engine))
            throw new ScenarioException(scenarioName, "Scenario " + scenarioName + " is not baked.");

        SessionCreator creator = this.registry.getScenarioManager().newSession();
        engine.getScenario().getTriggers().stream()
                .filter(t -> t.getType() == type)
                .forEach(t -> creator.add(engine, t.getType()));

        this.registry.getScenarioManager().queueScenario(creator);
    }

    private void registerActionTrigger(ScenarioEngine engine, TriggerBean actionTrigger)
    {
        ScenarioFileBean scenario = engine.getScenario();
        Plugin plugin = engine.getPlugin();

        TriggerArgument triggerArgument = actionTrigger.getArgument();
        if (!(triggerArgument instanceof ActionBean))
            throw new IllegalArgumentException("Action trigger argument required");

        ActionBean argument = (ActionBean) triggerArgument;

        CompiledAction<?> action = this.actionManager.getCompiler().compile(
                this.registry,
                engine,
                argument,
                null,  /* reportErrorTo */
                null  /* onSuccess */
        );

        // 型はコンパイラでチェック済みなのでキャストしておく。
        this.actionManager.queueWatch(
                plugin,
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
