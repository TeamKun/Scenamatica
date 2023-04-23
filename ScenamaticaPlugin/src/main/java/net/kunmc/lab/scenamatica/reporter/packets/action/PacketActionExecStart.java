package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PacketActionExecStart extends AbstractActionPacket
{
    private static final String KEY_TIMEOUT = "timeout";

    private static final String TYPE = "exec_start";

    private final long timeout;

    public PacketActionExecStart(@NotNull ScenarioEngine scenario, @NotNull CompiledScenarioAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action.getBean().getAction());

        this.timeout = action.getBean().getTimeout();
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_TIMEOUT, this.timeout);

        return result;
    }
}
