package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.jetbrains.annotations.NotNull;

public class PacketActionExpectSuccess extends AbstractActionPacket
{
    private static final String TYPE = "expect_success";

    public PacketActionExpectSuccess(@NotNull ScenarioEngine scenario, @NotNull CompiledAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action.getBean());
    }
}
