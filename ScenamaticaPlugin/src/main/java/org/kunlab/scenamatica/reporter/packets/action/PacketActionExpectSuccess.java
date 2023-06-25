package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

public class PacketActionExpectSuccess extends AbstractActionPacket
{
    private static final String TYPE = "expect_success";

    public PacketActionExpectSuccess(@NotNull ScenarioEngine scenario, @NotNull CompiledAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action);
    }
}
