package net.kunmc.lab.scenamatica.scenario.runtime;

import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionCompiler;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InternalCompiler
{
    public static List<CompiledScenarioAction<?>> compileActions(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ScenarioEngine engine,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull List<? extends ScenarioBean> scenarios)
    {
        List<CompiledScenarioAction<?>> compiled = new ArrayList<>();
        for (ScenarioBean scenario : scenarios)
            compiled.add(compileAction(registry, engine, compiler, listener, scenario));

        return compiled;
    }

    public static <A extends ActionArgument> CompiledScenarioAction<A> compileAction(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ScenarioEngine engine,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull ScenarioBean scenario)
    {
        CompiledAction<A> action = compiler.compile(
                registry,
                engine,
                scenario.getAction(),
                listener::onActionError,
                listener::onActionExecuted
        );

        if (scenario.getType() == ScenarioType.CONDITION_REQUIRE)
            if (!(action.getExecutor() instanceof Requireable<?>))
                throw new IllegalArgumentException("Action " + scenario.getAction().getType() + " is not requireable.");

        action.getExecutor().validateArgument(engine, scenario.getType(), action.getArgument());


        return new CompiledScenarioActionImpl<>(
                scenario,
                scenario.getType(),
                action,
                scenario.getRunIf() == null ? null: compileConditionAction(registry, engine, compiler, listener, scenario.getRunIf())
        );
    }

    public static <A extends ActionArgument> CompiledScenarioAction<A> compileConditionAction(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ScenarioEngine engine,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull ActionBean bean)
    {  // RunIF 用に偽装する。
        CompiledAction<A> action = compiler.compile(
                registry,
                engine,
                bean,
                listener::onActionError,
                listener::onActionExecuted
        );

        action.getExecutor().validateArgument(engine, ScenarioType.CONDITION_REQUIRE, action.getArgument());

        return new CompiledScenarioActionImpl<>(
                new ScenarioBean()
                {
                    @Override
                    public @NotNull ScenarioType getType()
                    {
                        return ScenarioType.CONDITION_REQUIRE;
                    }

                    @Override
                    public @NotNull ActionBean getAction()
                    {
                        return bean;
                    }

                    @Override
                    public ActionBean getRunIf()
                    {
                        return null;
                    }

                    @Override
                    public long getTimeout()
                    {
                        return -1;
                    }
                },
                ScenarioType.CONDITION_REQUIRE,
                action,
                null
        );
    }

    public static int calcCompileNeeded(ScenarioFileBean scenario)
    {
        int compileNeeded = 1; // 本シナリオの分。

        for (TriggerBean trigger : scenario.getTriggers())
        {
            if (!trigger.getBeforeThat().isEmpty())
                compileNeeded++;
            if (!trigger.getAfterThat().isEmpty())
                compileNeeded++;
        }

        if (scenario.getRunIf() != null)
            compileNeeded++;

        return compileNeeded;
    }
}
