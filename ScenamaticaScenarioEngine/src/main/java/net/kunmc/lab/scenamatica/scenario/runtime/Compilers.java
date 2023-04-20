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
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Compilers
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

    private static <A extends ActionArgument> CompiledScenarioAction<A> compileAction(
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
            if (!(action.getAction() instanceof Requireable<?>))
                throw new IllegalArgumentException("Action " + scenario.getAction().getType() + " is not requireable.");
            else if (action.getArgument() != null)
            {
                /* assert action instanceof Requireable<A>;*/
                // noinspection unchecked
                ((Requireable<A>) action.getAction()).validateArgument(action.getArgument());
            }

        return new CompiledScenarioActionImpl<>(
                scenario,
                scenario.getType(),
                action.getAction(),
                action.getArgument()
        );
    }
}
