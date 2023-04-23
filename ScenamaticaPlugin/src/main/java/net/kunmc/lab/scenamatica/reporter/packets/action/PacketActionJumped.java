package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.jetbrains.annotations.NotNull;

public class PacketActionJumped extends AbstractActionPacket
{
    private static final String TYPE = "jumped";

    private final CompiledAction<?> expected;

    public PacketActionJumped(@NotNull ScenarioEngine scenario, @NotNull CompiledAction<?> action, @NotNull CompiledAction<?> expected)
    {
        super(TYPE, scenario.getTestID(), action.getBean());
        this.expected = expected;
    }
}
