package net.kunmc.lab.scenamatica.reporter.packets.test;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class PacketTestEnd extends AbstractTestPacket
{
    private static final String KEY_STATE = "state";
    private static final String KEY_CAUSE = "cause";
    private static final String KEY_STARTED_AT = "startedAt";
    private static final String KEY_FINISHED_AT = "finishedAt";

    private static final String TYPE = "end";

    @NotNull
    TestState state;
    @NotNull
    TestResultCause cause;
    long startedAt;
    long finishedAt;

    public PacketTestEnd(@NotNull TestResult result)
    {
        super(TYPE, result.getTestID());

        this.state = result.getState();
        this.cause = result.getTestResultCause();
        this.startedAt = result.getStartedAt();
        this.finishedAt = result.getFinishedAt();
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

        result.put(KEY_STATE, this.state.name());
        result.put(KEY_CAUSE, this.cause.name());
        result.put(KEY_STARTED_AT, this.startedAt);
        result.put(KEY_FINISHED_AT, this.finishedAt);

        return result;
    }
}
