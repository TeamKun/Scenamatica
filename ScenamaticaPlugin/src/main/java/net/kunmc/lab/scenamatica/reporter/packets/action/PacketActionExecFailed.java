package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.jetbrains.annotations.NotNull;

public class PacketActionExecFailed extends AbstractActionPacket
{
    private static final String KEY_TIMEOUT = "timeout";

    private static final String TYPE = "exec_failed";

    public PacketActionExecFailed(@NotNull ScenarioEngine scenario, @NotNull CompiledAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action.getBean());
    }
}
