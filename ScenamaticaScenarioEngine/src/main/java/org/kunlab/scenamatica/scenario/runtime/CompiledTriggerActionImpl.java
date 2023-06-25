package org.kunlab.scenamatica.scenario.runtime;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;

import java.util.List;

@Value
public class CompiledTriggerActionImpl implements CompiledTriggerAction
{
    @NotNull
    TriggerBean trigger;
    @NotNull
    List<CompiledScenarioAction<?>> beforeActions;
    @NotNull
    List<CompiledScenarioAction<?>> afterActions;
    @Nullable
    CompiledScenarioAction<?> runIf;
}
