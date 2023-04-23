package net.kunmc.lab.scenamatica.reporter.packets.test;

import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBeanImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PacketTestSkip extends AbstractTestPacket
{
    private static final String TYPE = "skip";

    private static final String KEY_REASON_ACTION = "reason_action";

    @NotNull
    private final CompiledScenarioAction<?> action;

    public PacketTestSkip(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        super(TYPE, engine.getTestID());
        this.action = action;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_REASON_ACTION, ScenarioBeanImpl.serialize(this.action.getBean()));

        return result;
    }
}
