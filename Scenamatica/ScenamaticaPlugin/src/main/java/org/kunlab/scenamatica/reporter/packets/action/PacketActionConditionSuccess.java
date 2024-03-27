package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;

public class PacketActionConditionSuccess extends AbstractActionPacket
{
    private static final String TYPE = "condition_success";

    public PacketActionConditionSuccess(@NotNull ScenarioEngine scenario, @NotNull CompiledScenarioAction action)
    {
        super(TYPE, scenario.getTestID(), action.getAction());
    }
}
