package org.kunlab.scenamatica.reporter.packets.test;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;

import java.util.Map;

public class PacketTestSkip extends AbstractTestPacket
{
    public static final String KEY_REASON_ACTION = "reason_action";
    private static final String TYPE = "skip";
    private final ScenarioEngine engine;

    @NotNull
    private final CompiledScenarioAction action;

    public PacketTestSkip(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {
        super(TYPE, engine.getTestID());
        this.engine = engine;
        this.action = action;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_REASON_ACTION, this.engine.getManager().getRegistry().getScenarioFileManager()
                .getSerializer().serialize(this.action.getStructure(), ScenarioStructure.class));

        return result;
    }
}
