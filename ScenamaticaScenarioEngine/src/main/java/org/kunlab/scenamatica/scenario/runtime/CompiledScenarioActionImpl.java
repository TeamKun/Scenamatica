package org.kunlab.scenamatica.scenario.runtime;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;

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
