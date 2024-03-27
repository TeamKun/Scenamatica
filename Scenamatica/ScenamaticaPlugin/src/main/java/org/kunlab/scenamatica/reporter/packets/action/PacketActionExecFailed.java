package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

public class PacketActionExecFailed extends AbstractActionPacket
{
    private static final String TYPE = "exec_failed";

    public PacketActionExecFailed(@NotNull ScenarioEngine scenario, @NotNull CompiledAction action)
    {
        super(TYPE, scenario.getTestID(), action);
    }
}
