package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.jetbrains.annotations.NotNull;

public class PacketActionConditionChecking extends AbstractActionPacket
{
    private static final String TYPE = "condition_checking";

    public PacketActionConditionChecking(@NotNull ScenarioEngine scenario, @NotNull CompiledScenarioAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action.getBean().getAction());
    }
}
