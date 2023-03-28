package net.kunmc.lab.scenamatica.scenario.runtime;

import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;

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

}
