package org.kunlab.scenamatica.reporter.packets.test;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class PacketTestEnd extends AbstractTestPacket
{
    public static final String KEY_SCENARIO = "scenario";
    public static final String KEY_STATE = "state";
    public static final String KEY_CAUSE = "cause";
    public static final String KEY_STARTED_AT = "startedAt";
    public static final String KEY_FINISHED_AT = "finishedAt";
    public static final String KEY_ATTEMPT_OF = "attemptOf";

    private static final String TYPE = "end";

    @NotNull
    ScenarioFileStructure scenario;
    @NotNull
    ScenarioState state;
    @NotNull
    ScenarioResultCause cause;
    long startedAt;
    long finishedAt;
    int attemptOf;

    public PacketTestEnd(@NotNull ScenarioResult result)
    {
        super(TYPE, result.getTestID());

        this.scenario = result.getScenario();
        this.state = result.getState();
        this.cause = result.getScenarioResultCause();
        this.startedAt = result.getStartedAt();
        this.finishedAt = result.getFinishedAt();
        this.attemptOf = result.getAttemptOf();
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
        Map<String, Object> result = super.serialize();


        result.put(KEY_SCENARIO, StructureSerializerImpl.getInstance().serialize(this.scenario, ScenarioFileStructure.class));
        result.put(KEY_STATE, this.state.name());
        result.put(KEY_CAUSE, this.cause.name());
        result.put(KEY_STARTED_AT, this.startedAt);
        result.put(KEY_FINISHED_AT, this.finishedAt);
        result.put(KEY_ATTEMPT_OF, this.attemptOf);

        return result;
    }
}
