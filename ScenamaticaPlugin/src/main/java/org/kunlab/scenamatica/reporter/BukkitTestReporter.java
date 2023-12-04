package org.kunlab.scenamatica.reporter;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BukkitTestReporter extends AbstractTestReporter
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
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger)
    {
        ScenarioFileStructure scenario = engine.getScenario();

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
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> t.warn(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.skip",
                MsgArgs.of("scenario", scenario.getName())
                        .add("condition", getConditionString(action))
        ))));
    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileStructure scenario = engine.getScenario();

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
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.run.success",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
        ))));
    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.watch.done",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
        ))));

    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull Throwable error)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> t.info(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.run.fail",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
                        .add("cause", error.getClass().getSimpleName() + ": " + error.getMessage())
        ))));
    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull CompiledAction<?> expected)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> t.warn(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.jumped",
                MsgArgs.of("action", action.getExecutor().getClass().getSimpleName())
                        .add("scenario", engine.getScenario().getName())
        ))));
    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> t.success(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.require.success",
                MsgArgs.of("condition", getConditionString(action))
        ))));
    }

    @Override
    public void onConditionCheckFailed(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> t.error(this.withPrefix(engine.getTestID(), scenario, LangProvider.get(
                "test.action.require.fail",
                MsgArgs.of("condition", getConditionString(action))
        ))));
    }

    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> {
            UUID testID = result.getTestID();
            this.printSeparator(result.getTestID(), t, scenario, 12);
            this.printTestSummary(t, scenario, result);
            this.printSeparator(testID, t, scenario, 12);
            this.printDetails(t, scenario, result);
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

        this.terminals.forEach(t -> {
            if (failed > 0)
                this.printAutoRetryTip(t);
            this.printSessionSummary(t, results, elapsedStr, total, passed, failed, cancelled, skipped);
        });
    }

    private List<? extends ScenarioResult> pickupFlakes(List<? extends ScenarioResult> results)
    {
        return new ArrayList<>(results.stream()
                .filter(r -> r.getAttemptOf() > 1)
                .filter(r -> !r.getScenarioResultCause().isFailure())
                .collect(Collectors.toMap(
                        r -> Arrays.asList(r.getTestID(), r.getScenario()),
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparingInt(ScenarioResult::getAttemptOf))
                ))
                .values());
    }

    protected void printSessionSummary(@NotNull Terminal terminal, List<? extends ScenarioResult> results,
                                       String elapsedStr, int total, int passed, int failed, int cancelled, int skipped)
    {
        boolean allPassed = passed == total;
        boolean someFails = failed > 0;
        boolean noTests = cancelled + skipped == total;

        this.printSeparator(null, terminal, null, 50);

        terminal.writeLine("");

        List<? extends ScenarioResult> flakes = this.pickupFlakes(results);
        if (flakes.isEmpty())
            terminal.info(LangProvider.get(
                    "test.session.result.stats",
                    MsgArgs.of("totalRun", total - skipped - cancelled)
                            .add("passed", passed)
                            .add("failed", failed)
                            .add("cancelled", cancelled)
                            .add("skipped", skipped)
                            .add("elapsed", elapsedStr)
            ));
        else
        {
            long flakeScenarios = /* flaking runs = */ flakes.size();
            long attemptCount = flakes.stream().mapToLong(ScenarioResult::getAttemptOf).sum();
            terminal.info(LangProvider.get(
                    "test.session.result.stats.with_flakes",
                    MsgArgs.of("totalRun", total - skipped - cancelled)
                            .add("passed", passed)
                            .add("failed", failed)
                            .add("cancelled", cancelled)
                            .add("skipped", skipped)
                            .add("elapsed", elapsedStr)
                            .add("flakes", flakeScenarios)
                            .add("attempts", attemptCount)
            ));
        }


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

        if (failed != 0)
            this.printFailure(terminal, failed, results);

        this.printSeparator(null, terminal, null, 50);
    }

    private void printFailure(Terminal terminal, int failed, List<? extends ScenarioResult> results)
    {
        terminal.info(LangProvider.get(
                "test.session.result.failures",
                MsgArgs.of("count", failed)
        ));
        List<String> distinctMessages = results.stream()
                // 失敗のみを抽出
                .filter(r -> r.getScenarioResultCause().isFailure())
                // 失敗メッセージの基礎を作成
                .map(r -> {
                    String scenario = r.getScenario().getName();
                    String cause = r.getScenarioResultCause().name();
                    String action = (r.getFailedAction() == null) ? "???": r.getFailedAction().getName();
                    return LangProvider.get(
                            "test.session.result.failures.entry",
                            MsgArgs.of("scenario", scenario)
                                    .add("action", action)
                                    .add("cause", cause)
                    );
                })
                // 重複をカウントして保持
                .collect(Collectors.groupingByConcurrent(msg -> msg, Collectors.counting()))
                .entrySet().stream()
                // メッセージを構築
                .flatMap(entry -> {
                    long count = entry.getValue();
                    return IntStream.rangeClosed(1, (int) count)
                            .mapToObj(index -> (count > 1) ? entry.getKey() + " (" + index + ")": entry.getKey());
                })
                .collect(Collectors.toList());

        distinctMessages.forEach(terminal::writeLine);
    }

    protected void printTestSummary(Terminal terminal, ScenarioFileStructure scenario, ScenarioResult result)
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
            terminal.success(this.withPrefix(result.getTestID(), scenario, summary));
        else
            terminal.error(this.withPrefix(result.getTestID(), scenario, summary));
    }

    protected void printDetails(Terminal terminal, ScenarioFileStructure scenario, ScenarioResult result)
    {
        ScenarioResultCause cause = result.getScenarioResultCause();

        terminal.info(this.withPrefix(result.getTestID(), scenario, LangProvider.get("test.result.detail")));
        terminal.info(this.withPrefix(result.getTestID(), scenario, LangProvider.get(
                "test.result.detail.id",
                MsgArgs.of("id", result.getTestID().toString().substring(0, 8))
        )));
        if (cause != ScenarioResultCause.PASSED)
            terminal.info(this.withPrefix(result.getTestID(), scenario, LangProvider.get(
                    "test.result.detail.state",
                    MsgArgs.of("state", result.getState()
                    )
            )));

        long sAt = result.getStartedAt();
        long fAt = result.getFinishedAt();
        String startedAt = formatDateTime(sAt);
        String finishedAt = formatDateTime(fAt);
        String elapsed = formatTime(fAt - sAt);

        terminal.info(this.withPrefix(result.getTestID(), scenario, LangProvider.get(
                "test.result.detail.elapsed",
                MsgArgs.of("startedAt", startedAt)
                        .add("finishedAt", finishedAt)
                        .add("elapsed", elapsed)
        )));

        if (cause == ScenarioResultCause.PASSED || cause == ScenarioResultCause.CANCELLED || cause == ScenarioResultCause.SKIPPED
                || result.getFailedAction() == null)
            return;

        terminal.info(this.withPrefix(result.getTestID(), scenario, LangProvider.get(
                "test.result.detail.failed",
                MsgArgs.of("action", result.getFailedAction().getClass().getSimpleName())
        )));
    }

    protected void printSeparator(UUID testID, Terminal terminal, ScenarioFileStructure scenario)
    {
        if (terminal.isPlayer())
            this.printSeparator(testID, terminal, scenario, 25);
        else
            this.printSeparator(testID, terminal, scenario, 53);
    }

    protected void printSeparator(UUID testID, Terminal terminal, ScenarioFileStructure scenario, int size)
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

    protected String withPrefix(UUID testID, ScenarioFileStructure scenario, String message)
    {
        return LogUtils.gerScenarioPrefix(testID, scenario) + message;
    }

    protected void printAutoRetryTip(Terminal terminal)
    {
        terminal.hint("test.session.result.retry_tip");
    }
}
