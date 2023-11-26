package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;

import java.util.Map;

public class PacketActionExpectStart extends AbstractActionPacket
{
    public static final String KEY_TIMEOUT = "timeout";

    private static final String TYPE = "expect_start";

    private final long timeout;

    public PacketActionExpectStart(@NotNull ScenarioEngine scenario, @NotNull CompiledScenarioAction<?> action)
    {
        super(TYPE, scenario.getTestID(), action.getAction());

        this.timeout = action.getStructure().getTimeout();
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_TIMEOUT, this.timeout);

        return result;
    }
}
