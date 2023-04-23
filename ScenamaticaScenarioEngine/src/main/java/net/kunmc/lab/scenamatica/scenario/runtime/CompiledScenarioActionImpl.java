package net.kunmc.lab.scenamatica.scenario.runtime;

import lombok.Value;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class CompiledScenarioActionImpl<A extends ActionArgument> implements CompiledScenarioAction<A>
{
    @NotNull
    ScenarioBean bean;
    @NotNull
    ScenarioType type;
    @NotNull
    CompiledAction<A> action;
    @Nullable
    CompiledScenarioAction<?> runIf;
}
