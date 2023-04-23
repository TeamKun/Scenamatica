package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.jetbrains.annotations.NotNull;

public class PacketActionConditionSuccess extends AbstractActionPacket
{
    private static final String TYPE = "condition_success";

    public PacketActionConditionSuccess(@NotNull ScenarioEngine scenario, @NotNull CompiledScenarioAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action.getAction());
    }
}
