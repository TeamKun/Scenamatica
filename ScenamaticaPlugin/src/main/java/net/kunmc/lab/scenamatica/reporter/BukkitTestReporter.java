package net.kunmc.lab.scenamatica.reporter;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.scenamatica.commons.utils.LogUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioResultCause;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.QueuedScenario;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
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
import java.util.stream.Collectors;

public class BukkitTestReporter implements net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter
{
    protected final List<Terminal> terminals;

    public BukkitTestReporter()
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

    private static String getConditionString(@NotNull CompiledScenarioAction<?> action)
    {
        String condition = action.getAction().getExecutor().getClass().getSimpleName();
        if (action.getAction().getArgument() != null)
            condition += " - " + action.getAction().getArgument().getArgumentString();

        return condition;
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

        this.terminals.forEach(t ->
                t.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                        "test.start",
                        MsgArgs.of("scenario", scenario.getName())
                                .add("trigger", trigger.getType().name())
                )))
        );
    }

    @Override
    public void onTestSkipped(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.warn(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.skip",
                MsgArgs.of("scenario", scenario.getName())
                        .add("condition", getConditionString(action))
        ))));
    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        switch (action.getType())
        {
            case ACTION_EXECUTE:
                this.terminals.forEach(t -> t.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                        "test.action.run",
                        MsgArgs.of("action", action.getAction().getExecutor().getClass().getSimpleName())
                ))));
                break;
            case ACTION_EXPECT:
                this.terminals.forEach(t -> t.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                        "test.action.watch",
                        MsgArgs.of("action", action.getAction().getExecutor().getClass().getSimpleName())
                ))));
                break;
            case CONDITION_REQUIRE:
                this.terminals.forEach(t -> t.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                        "test.action.require.start",
                        MsgArgs.of("condition", getConditionString(action))
                ))));
        }
    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.run.success",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
        ))));
    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.watch.done",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
        ))));

    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull Throwable error)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.run.fail",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
                        .add("cause", error.getClass().getSimpleName() + ": " + error.getMessage())
        ))));
    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull CompiledAction<?> expected)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.warn(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.jumped",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
                        .add("scenario", engine.getScenario().getName())
        ))));
    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.require.success",
                MsgArgs.of("condition", getConditionString(action))
        ))));
    }

    @Override
    public void onConditionCheckFailed(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> t.error(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.require.fail",
                MsgArgs.of("condition", getConditionString(action))
        ))));
    }

    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {
        ScenarioFileBean scenario = engine.getScenario();

        this.terminals.forEach(t -> {
            UUID testID = engine.getTestID();
            this.printSeparator(engine.getTestID(), t, scenario, 12);
            this.printTestSummary(engine, t, scenario, result);
            this.printSeparator(testID, t, scenario, 12);
            this.printDetails(engine, t, scenario, result);
            this.printSeparator(testID, t, scenario, 12);
        });
    }

    @Override
    public void onTestSessionStart(@NotNull ScenarioSession session)
    {
        this.terminals.forEach(t -> {
            this.printSeparator(null, t, null, 50);
            t.info(ChatColor.AQUA + " T E S T S");
            this.printSeparator(null, t, null, 50);

        });
    }

    @Override
    public void onTestSessionEnd(@NotNull ScenarioSession session)
    {
        long endedAt = System.currentTimeMillis();
        long elapsed = endedAt - session.getStartedAt();
        String elapsedStr = formatTime(elapsed);
        List<ScenarioResult> results = session.getScenarios().stream()
                .map(QueuedScenario::getResult)
                .collect(Collectors.toList());

        int total = results.size();
        int passed = (int) results.stream().parallel()
                .filter(r -> r.getScenarioResultCause() == ScenarioResultCause.PASSED).count();
        int failed = (int) results.stream().parallel()
                .map(ScenarioResult::getScenarioResultCause)
                .filter(ScenarioResultCause::isFailure)
                .count();
        int cancelled = (int) results.stream().parallel()
                .filter(r -> r.getScenarioResultCause() == ScenarioResultCause.CANCELLED).count();
        int skipped = (int) results.stream().parallel()
                .filter(r -> r.getScenarioResultCause() == ScenarioResultCause.SKIPPED).count();

        this.terminals.forEach(t -> this.printSessionSummary(t, elapsedStr, total, passed, failed, cancelled, skipped));
    }

    protected void printSessionSummary(@NotNull Terminal terminal,
                                       String elapsedStr, int total, int passed, int failed, int cancelled, int skipped)
    {
        boolean allPassed = passed == total;
        boolean someFails = failed > 0;
        boolean noTests = cancelled + skipped == total;

        this.printSeparator(null, terminal, null, 50);

        terminal.writeLine("");

        terminal.info(LangProvider.get(
                "test.session.result.stats",
                MsgArgs.of("total", total)
                        .add("passed", passed)
                        .add("failed", failed)
                        .add("cancelled", cancelled)
                        .add("skipped", skipped)
                        .add("elapsed", elapsedStr)
        ));

        terminal.writeLine("");

        String resultKey = null;
        String messageKey = null;
        if (allPassed)
        {
            resultKey = "test.result.passed";
            messageKey = "test.session.result.message.passed";
        }
        else if (someFails)
        {
            resultKey = "test.result.failed";
            messageKey = "test.session.result.message.failed";
        }
        else if (noTests)
        {
            resultKey = "test.result.unknown";
            messageKey = "test.session.result.message.noTests";
        }

        if (resultKey != null)
        {
            String summary = LangProvider.get(
                    "test.result",
                    MsgArgs.of("result", LangProvider.get(resultKey))
                            .add("message", "%%" + messageKey + "%%")
            );

            if (allPassed)
                terminal.success(summary);
            else
                terminal.error(summary);

            terminal.writeLine("");
        }


        this.printSeparator(null, terminal, null, 50);
    }

    protected void printTestSummary(ScenarioEngine engine, Terminal terminal, ScenarioFileBean scenario, ScenarioResult result)
    {
        boolean passed = result.getScenarioResultCause() == ScenarioResultCause.PASSED;

        String resultKey;
        if (passed)
            resultKey = "test.result.passed";
        else if (result.getScenarioResultCause() == ScenarioResultCause.CANCELLED)
            resultKey = "test.result.cancelled";
        else if (result.getScenarioResultCause() == ScenarioResultCause.SKIPPED)
            resultKey = "test.result.skipped";
        else
            resultKey = "test.result.failed";
        String messageKey = "test.result.message." + result.getScenarioResultCause().name().toLowerCase();

        String summary = LangProvider.get(
                "test.result",
                MsgArgs.of("result", LangProvider.get(resultKey)).add("message", "%%" + messageKey + "%%")
        );
        if (passed)
            terminal.success(this.withPrefix(engine.getTestID(), scenario, summary));
        else
            terminal.error(this.withPrefix(engine.getTestID(), scenario, summary));
    }

    protected void printDetails(ScenarioEngine engine, Terminal terminal, ScenarioFileBean scenario, ScenarioResult result)
    {
        ScenarioResultCause cause = result.getScenarioResultCause();

        terminal.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get("test.result.detail")));
        terminal.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.result.detail.id",
                MsgArgs.of("id", result.getTestID().toString().substring(0, 8))
        )));
        if (cause != ScenarioResultCause.PASSED)
            terminal.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                    "test.result.detail.state",
                    MsgArgs.of("state", result.getState()
                    )
            )));

        long sAt = result.getStartedAt();
        long fAt = result.getFinishedAt();
        String startedAt = formatDateTime(sAt);
        String finishedAt = formatDateTime(fAt);
        String elapsed = formatTime(fAt - sAt);

        terminal.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.result.detail.elapsed",
                MsgArgs.of("startedAt", startedAt)
                        .add("finishedAt", finishedAt)
                        .add("elapsed", elapsed)
        )));

        if (cause == ScenarioResultCause.PASSED || cause == ScenarioResultCause.CANCELLED || cause == ScenarioResultCause.SKIPPED
                || result.getFailedAction() == null)
            return;

        terminal.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.result.detail.failed",
                MsgArgs.of("action", result.getFailedAction().getClass().getSimpleName())
        )));
    }

    protected void printSeparator(UUID testID, Terminal terminal, ScenarioFileBean scenario)
    {
        if (terminal.isPlayer())
            this.printSeparator(testID, terminal, scenario, 25);
        else
            this.printSeparator(testID, terminal, scenario, 53);
    }

    protected void printSeparator(UUID testID, Terminal terminal, ScenarioFileBean scenario, int size)
    {
        int maxSize = 53;
        if (size > maxSize)
            size = maxSize;
        int centerSpace = (maxSize - size) / 2;

        String center = StringUtils.repeat(" ", centerSpace);
        String separator = StringUtils.repeat("-", size);

        String line = center + separator;

        terminal.info(this.withPrefix(testID, scenario, ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + line));
    }

    protected String withPrefix(UUID testID, ScenarioFileBean scenario, String message)
    {
        return LogUtils.gerScenarioPrefix(testID, scenario) + message;
    }

}
