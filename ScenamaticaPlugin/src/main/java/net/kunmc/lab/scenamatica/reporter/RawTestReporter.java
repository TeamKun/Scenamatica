package net.kunmc.lab.scenamatica.reporter;

import com.google.gson.Gson;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.reporter.packets.AbstractRawPacket;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionConditionCheckFailed;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionConditionChecking;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionConditionSuccess;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionExecFailed;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionExecStart;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionExecuted;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionExpectStart;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionExpectSuccess;
import net.kunmc.lab.scenamatica.reporter.packets.action.PacketActionJumped;
import net.kunmc.lab.scenamatica.reporter.packets.session.PacketSessionEnd;
import net.kunmc.lab.scenamatica.reporter.packets.session.PacketSessionStart;
import net.kunmc.lab.scenamatica.reporter.packets.test.PacketTestEnd;
import net.kunmc.lab.scenamatica.reporter.packets.test.PacketTestSkip;
import net.kunmc.lab.scenamatica.reporter.packets.test.PacketTestStart;
import org.jetbrains.annotations.NotNull;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

public class RawTestReporter implements TestReporter
{
    private static final Gson GSON = new Gson();
    private static final PrintStream OUT = new PrintStream(new FileOutputStream(FileDescriptor.out));

    @Override
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerBean trigger)
    {
        this.printJSON(new PacketTestStart(engine));
    }

    @Override
    public void onTestSkipped(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        this.printJSON(new PacketTestSkip(engine, action));
    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        switch (action.getType())
        {
            case ACTION_EXECUTE:
                this.printJSON(new PacketActionExecStart(engine, action));
                break;
            case ACTION_EXPECT:
                this.printJSON(new PacketActionExpectStart(engine, action));
                break;
            case CONDITION_REQUIRE:
                this.printJSON(new PacketActionConditionChecking(engine, action));
                break;
        }
    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        this.printJSON(new PacketActionExecuted(engine, action));
    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        this.printJSON(new PacketActionExpectSuccess(engine, action));
    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull CompiledAction<?> expected)
    {
        this.printJSON(new PacketActionJumped(engine, action, expected));
    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull Throwable error)
    {
        this.printJSON(new PacketActionExecFailed(engine, action));
    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        this.printJSON(new PacketActionConditionSuccess(engine, action));
    }

    @Override
    public void onConditionCheckFailed(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        this.printJSON(new PacketActionConditionCheckFailed(engine, action));
    }

    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {
        this.printJSON(new PacketTestEnd(result));
    }

    @Override
    public void onTestSessionStart(@NotNull ScenarioSession session)
    {
        this.printJSON(new PacketSessionStart(session));
    }

    @Override
    public void onTestSessionEnd(@NotNull ScenarioSession session)
    {
        this.printJSON(new PacketSessionEnd(session));
    }

    private void printJSON(@NotNull AbstractRawPacket packet)
    {
        Map<String, Object> map = packet.serialize();
        String json = GSON.toJson(map);
        OUT.println(json);
    }
}
