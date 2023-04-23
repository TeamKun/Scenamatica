package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.jetbrains.annotations.NotNull;

public class PacketActionExecuted extends AbstractActionPacket
{
    private static final String TYPE = "exec_success";

    public PacketActionExecuted(@NotNull ScenarioEngine scenario, @NotNull CompiledAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action.getBean());
    }
}
