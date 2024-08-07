package org.kunlab.scenamatica.scenario.runtime;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;

@Value
public class CompiledScenarioActionImpl implements CompiledScenarioAction
{
    @NotNull
    ScenarioStructure structure;
    @NotNull
    ScenarioType type;
    @NotNull
    CompiledAction action;
    @Nullable
    CompiledScenarioAction runIf;
}
