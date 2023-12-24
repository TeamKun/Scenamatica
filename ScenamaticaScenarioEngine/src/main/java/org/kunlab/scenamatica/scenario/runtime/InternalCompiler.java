package org.kunlab.scenamatica.scenario.runtime;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.ArrayList;
import java.util.List;

public class InternalCompiler
{
    public static List<CompiledScenarioAction> compileActions(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ScenarioEngine engine,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull List<? extends ScenarioStructure> scenarios)
    {
        List<CompiledScenarioAction> compiled = new ArrayList<>();
        for (ScenarioStructure scenario : scenarios)
            compiled.add(compileAction(registry, engine, compiler, listener, scenario));

        return compiled;
    }

    public static CompiledScenarioAction compileAction(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ScenarioEngine engine,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull ScenarioStructure scenario)
    {
        try
        {
            CompiledAction action = compiler.compile(
                    registry,
                    engine,
                    scenario.getType(),
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

    public static CompiledScenarioAction compileConditionAction(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ScenarioEngine engine,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull ActionStructure structure)
    {  // RunIF 用に偽装する。
        CompiledAction action;
        try
        {

            action = compiler.compile(
                    registry,
                    engine,
                    ScenarioType.CONDITION_REQUIRE,
                    structure,
                    listener::onActionError,
                    listener::onActionExecuted
            );
        }

        catch (Throwable e)
        {
            throw new ScenarioCompilationErrorException(e, engine.getScenario().getName(), structure.getType());
        }

        return new CompiledScenarioActionImpl<>(
                new ScenarioStructure()
                {
                    @Override
                    public @NotNull ScenarioType getType()
                    {
                        return ScenarioType.CONDITION_REQUIRE;
                    }

                    @Override
                    public @NotNull ActionStructure getAction()
                    {
                        return structure;
                    }

                    @Override
                    public ActionStructure getRunIf()
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

    public static int calcCompileNeeded(ScenarioFileStructure scenario)
    {
        int compileNeeded = 1; // 本シナリオの分。

        for (TriggerStructure trigger : scenario.getTriggers())
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
