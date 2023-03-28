package net.kunmc.lab.scenamatica.scenario.runtime;

import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionCompiler;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Compilers
{
    public static List<CompiledScenarioAction<?>> compileActions(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull List<? extends ScenarioBean> scenarios)
    {
        List<CompiledScenarioAction<?>> compiled = new ArrayList<>();
        for (ScenarioBean scenario : scenarios)
            compiled.add(compileAction(registry, compiler, listener, scenario));

        return compiled;
    }

    private static <A extends ActionArgument> CompiledScenarioAction<A> compileAction(
            @NotNull ScenamaticaRegistry registry,
            @NotNull ActionCompiler compiler,
            @NotNull ScenarioActionListener listener,
            @NotNull ScenarioBean scenario)
    {
        CompiledAction<A> action = compiler.compile(
                registry,
                scenario.getAction(),
                listener::onActionError,
                listener::onActionExecuted
        );

        return new CompiledScenarioActionImpl<>(
                scenario,
                scenario.getType(),
                action.getAction(),
                action.getArgument()
        );
    }
}
