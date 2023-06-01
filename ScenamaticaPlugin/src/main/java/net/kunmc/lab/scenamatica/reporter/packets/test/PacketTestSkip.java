package net.kunmc.lab.scenamatica.reporter.packets.test;

import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PacketTestSkip extends AbstractTestPacket
{
    private static final String TYPE = "skip";

    private static final String KEY_REASON_ACTION = "reason_action";

    private final ScenarioEngine engine;

    @NotNull
    private final CompiledScenarioAction<?> action;

    public PacketTestSkip(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
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
                .getSerializer().serializeScenario(this.action.getBean()));

        return result;
    }
}
