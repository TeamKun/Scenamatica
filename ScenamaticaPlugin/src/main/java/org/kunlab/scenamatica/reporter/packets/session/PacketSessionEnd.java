package org.kunlab.scenamatica.reporter.packets.session;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.reporter.packets.test.PacketTestEnd;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PacketSessionEnd extends AbstractSessionPacket
{
    private static final String KEY_RESULTS = "results";
    private static final String KEY_STARTED_AT = "startedAt";
    private static final String TYPE = "end";

    @NotNull
    private final List<PacketTestEnd> tests;
    private final long startedAt;

    public PacketSessionEnd(@NotNull ScenarioSession session)
    {
        super(TYPE);
        this.tests = session.getScenarios().stream()
                .map(QueuedScenario::getResult)
                .map(PacketTestEnd::new)
                .collect(Collectors.toList());
        this.startedAt = session.getStartedAt();
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_RESULTS, this.tests.stream()
                .map(PacketTestEnd::serializeThis)
                .collect(Collectors.toList()));
        result.put(KEY_STARTED_AT, this.startedAt);

        return result;
    }
}
