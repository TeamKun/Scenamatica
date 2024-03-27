package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.ActionMetaUtils;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Map;

public class PacketActionJumped extends AbstractActionPacket
{
    public static final String KEY_EXPECTED = "expected";

    private static final String TYPE = "jumped";

    private final CompiledAction expected;

    public PacketActionJumped(@NotNull ScenarioEngine scenario, @NotNull ActionResult action, @NotNull CompiledAction expected)
    {
        super(TYPE, scenario.getTestID(), action);
        this.expected = expected;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_EXPECTED, this.serializeAction(ActionMetaUtils.getActionName(this.expected.getExecutor())));

        return result;
    }
}
