package net.kunmc.lab.scenamatica.reporter.packets.test;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class PacketTestStart extends AbstractTestPacket
{
    private static final String KEY_AUTO_START = "isAutoStart";
    private static final String KEY_STARTED_AT = "startedAt";
    private static final String KEY_SCENARIO = "scenario";

    private static final String TYPE = "start";

    @Getter(AccessLevel.NONE)
    ScenarioEngine engine;

    @NotNull
    ScenarioFileBean scenario;
    boolean isAutoStart;
    long startedAt;

    public PacketTestStart(@NotNull ScenarioEngine engine)
    {
        super(TYPE, engine.getTestID());
        this.engine = engine;
        this.scenario = engine.getScenario();
        this.isAutoStart = engine.isAutoRun();
        this.startedAt = engine.getStartedAt();
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.putAll(this.serializeThis());

        return result;
    }

    public Map<String, Object> serializeThis()
    {
        Map<String, Object> result = new HashMap<>();

        result.put(KEY_SCENARIO, this.engine.getManager().getRegistry().getScenarioFileManager().getSerializer()
                .serializeScenarioFile(this.scenario));
        result.put(KEY_AUTO_START, this.isAutoStart);
        result.put(KEY_STARTED_AT, this.startedAt);

        return result;

    }
}
