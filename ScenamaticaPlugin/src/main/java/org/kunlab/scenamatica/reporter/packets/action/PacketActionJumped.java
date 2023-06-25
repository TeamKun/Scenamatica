package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Map;

public class PacketActionJumped extends AbstractActionPacket
{
    private static final String KEY_EXPECTED = "expected";

    private static final String TYPE = "jumped";

    private final CompiledAction<?> expected;

    public PacketActionJumped(@NotNull ScenarioEngine scenario, @NotNull CompiledAction<?> action, @NotNull CompiledAction<?> expected)
    {
        super(TYPE, scenario.getTestID(), action);
        this.expected = expected;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_EXPECTED, this.serializeAction(this.expected));

        return result;
    }
}
