package net.kunmc.lab.scenamatica.reporter.packets.session;

import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import net.kunmc.lab.scenamatica.reporter.packets.test.PacketTestEnd;
import org.jetbrains.annotations.NotNull;

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

    public PacketSessionEnd(@NotNull List<? extends TestResult> tests, long startedAt)
    {
        super(TYPE);
        this.tests = tests.stream()
                .map(PacketTestEnd::new)
                .collect(Collectors.toList());
        this.startedAt = startedAt;
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
