package org.kunlab.scenamatica.scenario.runtime;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;

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
        try
        {
            CompiledAction<A> action = compiler.compile(
                    registry,
                    engine,
                    scenario.getAction(),
                    listener::onActionError,
                    listener::onActionExecuted
            );

            scenario.getType().validatePerformableActionType(action.getExecutor().getClass());
            if (action.getArgument() != null)
                action.getArgument().validate(engine, scenario.getType());


            return new CompiledScenarioActionImpl<>(
                    scenario,
                    scenario.getType(),
                    action,
                    scenario.getRunIf() == null ? null:
                            compileConditionAction(registry, engine, compiler, listener, scenario.getRunIf())
            );
        }
        catch (Throwable e)
        {
            throw new ScenarioCompilationErrorException(e, engine.getScenario().getName(), scenario.getAction().getType());
        }
    }

    public static <A extends ActionArgument> CompiledScenarioAction<A> compileConditionAction(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ScenarioEngine engine,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull ActionBean bean)
    {  // RunIF 用に偽装する。
        CompiledAction<A> action;
        try
        {

            action = compiler.compile(
                    registry,
                    engine,
                    bean,
                    listener::onActionError,
                    listener::onActionExecuted
            );
        }

        catch (Throwable e)
        {
            throw new ScenarioCompilationErrorException(e, engine.getScenario().getName(), bean.getType());
        }

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
