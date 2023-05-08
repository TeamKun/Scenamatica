package net.kunmc.lab.scenamatica.reporter.packets.session;

import net.kunmc.lab.scenamatica.interfaces.scenario.QueuedScenario;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import net.kunmc.lab.scenamatica.reporter.packets.test.PacketTestStart;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PacketSessionStart extends AbstractSessionPacket
{
    private static final String KEY_TESTS = "tests";

    private static final String TYPE = "start";

    @NotNull
    private final List<PacketTestStart> tests;

    public PacketSessionStart(@NotNull ScenarioSession session)
    {
        super(TYPE);
        this.tests = session.getScenarios().stream()
                .map(QueuedScenario::getEngine)
                .map(PacketTestStart::new)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_TESTS, this.tests.stream()
                .map(PacketTestStart::serializeThis)
                .collect(Collectors.toList()));

        return result;
    }
}
