package net.kunmc.lab.scenamatica;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TestReportRecipient implements TestReporter
{
    private final List<Terminal> terminals;

    public TestReportRecipient()
    {
        this.terminals = new ArrayList<>();

        this.terminals.add(Terminals.ofConsole());
    }

    private static String formatDateTime(long time)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

    private static String formatTime(long time)
    {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(time));
    }

    public void addRecipient(Terminal terminal)
    {
        this.terminals.add(terminal);
    }

    public void removeRecipient(Terminal terminal)
    {
        this.terminals.remove(terminal);
    }

    @Override
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerBean trigger)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> {
            printSeparator(engine.getTestID(), t, scenario);
            t.info(withPrefix(engine.getTestID(), scenario, ChatColor.AQUA + " T E S T"));
            printSeparator(engine.getTestID(), t, scenario);

            t.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                    "test.start",
                    MsgArgs.of("scenario", scenario.getName())
                            .add("trigger", trigger.getType().name())
            )));
        });
    }

    @Override
    public void onActionWatchStart(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.watch",
                MsgArgs.of("action", action.getAction().getClass().getSimpleName())
        ))));
    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.run",
                MsgArgs.of("action", action.getAction().getClass().getSimpleName())
        ))));
    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.run.success",
                MsgArgs.of("action", action.getAction().getClass().getSimpleName())
        ))));
    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull Action<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.watch.done",
                MsgArgs.of("action", action.getClass().getSimpleName())
        ))));

    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull Action<?> action, @NotNull CompiledScenarioAction<?> expected)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.warn(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.jumped",
                MsgArgs.of("action", action.getClass().getSimpleName())
        ))));
    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull Throwable error)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.run.fail",
                MsgArgs.of("action", action.getAction().getClass().getSimpleName())
                        .add("cause", error.getClass().getSimpleName() + ": " + error.getMessage())
        ))));
    }

    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull TestResult result)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> {
            UUID testID = engine.getTestID();
            printTestSummary(engine, t, scenario, result);
            printSeparator(testID, t, scenario, 26);
            printDetails(engine, t, scenario, result);
            printSeparator(testID, t, scenario, 26);
        });
    }

    private void printTestSummary(ScenarioEngine engine, Terminal terminal, ScenarioFileBean scenario, TestResult result)
    {
        boolean passed = result.getTestResultCause() == TestResultCause.PASSED;
        printSeparator(engine.getTestID(), terminal, scenario, 26);

        String resultKey;
        if (passed)
            resultKey = "test.result.passed";
        else if (result.getTestResultCause() == TestResultCause.CANCELLED)
            resultKey = "test.result.cancelled";
        else
            resultKey = "test.result.failed";
        String messageKey = "test.result.message." + result.getTestResultCause().name().toLowerCase();

        String summary = LangProvider.get(
                "test.result",
                MsgArgs.of("result", LangProvider.get(resultKey)).add("message", "%%" + messageKey + "%%")
        );
        if (passed)
            terminal.success(withPrefix(engine.getTestID(), scenario, summary));
        else
            terminal.error(withPrefix(engine.getTestID(), scenario, summary));
    }

    private void printDetails(ScenarioEngine engine, Terminal terminal, ScenarioFileBean scenario, TestResult result)
    {
        TestResultCause cause = result.getTestResultCause();

        terminal.info(withPrefix(engine.getTestID(), scenario, LangProvider.get("test.result.detail")));
        terminal.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.result.detail.id",
                MsgArgs.of("id", result.getTestID().toString().substring(0, 8))
        )));
        if (cause != TestResultCause.PASSED)
            terminal.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                    "test.result.detail.state",
                    MsgArgs.of("state", result.getState()
                    )
            )));

        long sAt = result.getStartedAt();
        long fAt = result.getFinishedAt();
        String startedAt = formatDateTime(sAt);
        String finishedAt = formatDateTime(fAt);
        String elapsed = formatTime(fAt - sAt);

        terminal.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.result.detail.elapsed",
                MsgArgs.of("startedAt", startedAt)
                        .add("finishedAt", finishedAt)
                        .add("elapsed", elapsed)
        )));

        if (cause == TestResultCause.PASSED || cause == TestResultCause.CANCELLED || cause == TestResultCause.SKIPPED
                || result.getFailedAction() == null)
            return;

        terminal.info(withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.result.detail.failed",
                MsgArgs.of("action", result.getFailedAction().getClass().getSimpleName())
        )));
    }

    private void printSeparator(UUID testID, Terminal terminal, ScenarioFileBean scenario)
    {
        printSeparator(testID, terminal, scenario, 53);
    }

    private void printSeparator(UUID testID, Terminal terminal, ScenarioFileBean scenario, int size)
    {
        int maxSize = 53;
        if (size > maxSize)
            size = maxSize;
        int centerSpace = (maxSize - size) / 2;

        String center = StringUtils.repeat(" ", centerSpace);
        String separator = StringUtils.repeat("-", size);

        String line = center + separator;

        terminal.info(withPrefix(testID, scenario, ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + line));
    }

    private String withPrefix(UUID testID, ScenarioFileBean scenario, String message)
    {
        return "[" +
                ChatColor.BOLD + ChatColor.YELLOW + "TEST-" + StringUtils.substring(scenario.getName(), 0, 8) +
                ChatColor.RESET + "/" + ChatColor.GRAY + testID.toString().substring(0, 8) +
                ChatColor.WHITE + "] " + message;
    }

}
