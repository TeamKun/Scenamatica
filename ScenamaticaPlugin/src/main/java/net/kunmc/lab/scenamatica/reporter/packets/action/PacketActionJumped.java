package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.jetbrains.annotations.NotNull;

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
