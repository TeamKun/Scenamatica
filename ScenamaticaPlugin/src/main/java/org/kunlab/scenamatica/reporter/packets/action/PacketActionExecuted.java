package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

public class PacketActionExecuted extends AbstractActionPacket
{
    private static final String TYPE = "exec_success";

    public PacketActionExecuted(@NotNull ScenarioEngine scenario, @NotNull CompiledAction action)
    {
        super(TYPE, scenario.getTestID(), action);
    }
}
