package net.kunmc.lab.scenamatica.trigger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.interfaces.trigger.TriggerManager;
import net.kunmc.lab.scenamatica.trigger.arguments.ActionTriggerArgument;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class TriggerManagerImpl implements TriggerManager
{
    static
    {
        TriggerType.ON_ACTION.setArgumentType(ActionTriggerArgument.class);
    }

    private final ScenamaticaRegistry registry;
    private final ActionManager actionManager;
    private final Multimap<String, TriggerBean> triggers;  // シナリオ名 / トリガー

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
        if (this.triggers.containsKey(scenarioName))
            throw new IllegalArgumentException("The scenario " + scenarioName + " is already baked.");

        this.triggers.putAll(scenarioName, scenario.getTriggers());

        // トリガのアクションを登録する。
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
        String key = scenarioName.toLowerCase(Locale.ROOT);
        if (!this.triggers.containsKey(key))
            throw new IllegalArgumentException("The scenario " + scenarioName + " is not baked.");

        for (TriggerBean trigger : this.triggers.get(key))
        {
            if (trigger.getType() != type)
                continue;

            this.registry.getScenarioManager().queueScenario(plugin, scenarioName, trigger.getType());
        }
    }

    private void registerActionTrigger(ScenarioEngine engine, TriggerBean actionTrigger)
    {
        ScenarioFileBean scenario = engine.getScenario();
        Plugin plugin = engine.getPlugin();

        TriggerArgument triggerArgument = actionTrigger.getArgument();
        if (!(triggerArgument == null || triggerArgument instanceof ActionBean))
            throw new IllegalArgumentException("Action trigger argument must be ActionArgument.");

        ActionBean argument = (ActionBean) triggerArgument;
        assert argument != null;

        CompiledAction<?> action = this.actionManager.getCompiler().compile(
                this.registry,
                engine,
                argument,
                null,  /* reportErrorTo */
                null  /* onSuccess */
        );

        // 型はコンパイラでチェック済みなのでキャストしておく。
        //noinspection unchecked,rawtypes
        this.actionManager.queueWatch(
                plugin,
                engine,
                scenario,
                (Action) action.getAction(),
                WatchType.TRIGGER,
                action.getArgument() // warn: unchecked,rawtypes
        );
    }
}
