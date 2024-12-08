package org.kunlab.scenamatica.reporter;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.ActionMetaUtils;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.enums.ActionResultCause;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitTestReporter extends AbstractTestReporter
{
    protected final List<Terminal> terminals;
    private final int maxAttempts;

    public BukkitTestReporter(int maxAttempts)
    {
        this.maxAttempts = maxAttempts;

        this.terminals = new ArrayList<>();

        this.terminals.add(Terminals.ofConsole());
    }

    public BukkitTestReporter()
    {
        this(1);
    }

    private void broadcastInfo(String messageKey, MsgArgs args)
    {
        String text = LangProvider.get(messageKey, args);
        this.terminals.forEach(t -> t.info(text));
    }

    private void broadcastScenarioInfo(String messageKey, ScenarioEngine engine, MsgArgs args)
    {
        String text = this.createScenarioMessage(engine, engine.getScenario(), messageKey, args);
        this.terminals.forEach(t -> t.info(text));
    }

    private void broadcastScenarioWarn(String messageKey, ScenarioEngine engine, MsgArgs args)
    {
        String text = this.createScenarioMessage(engine, engine.getScenario(), messageKey, args);
        this.terminals.forEach(t -> t.warn(text));
    }

    private void broadcastScenarioSuccess(String messageKey, ScenarioEngine engine, MsgArgs args)
    {
        String text = this.createScenarioMessage(engine, engine.getScenario(), messageKey, args);
        this.terminals.forEach(t -> t.success(text));
    }

    private void broadcastScenarioError(String messageKey, ScenarioEngine engine, MsgArgs args)
    {
        String text = this.createScenarioMessage(engine, engine.getScenario(), messageKey, args);
        this.terminals.forEach(t -> t.error(text));
    }

    private String createScenarioMessage(ScenarioEngine engine, ScenarioFileStructure scenario, String messageKey, MsgArgs args)
    {
        args.add("scenario", scenario.getName());
        return this.withPrefix(engine.getTestID(), scenario, LangProvider.get(messageKey, args));
    }

    private static String formatDateTime(long time)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

    private static String formatTime(long time)
    {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(time));
    }

    private static String getConditionString(@NotNull CompiledScenarioAction action)
    {
        String condition = action.getAction().getExecutor().getClass().getSimpleName();
        String argument = action.getAction().getContext().getInput().getValuesString();

        return argument.isEmpty() ? condition: condition + " - " + argument;
    }

    private static MsgArgs getSummaryStatsMsgArgsBase(ScenarioResultSet resultSet)
    {
        return MsgArgs.of("totalRun", resultSet.getTotalRan())
                .add("passed", resultSet.getCountPasses())
                .add("failed", resultSet.getCountFailures())
                .add("cancelled", resultSet.getCountCancels())
                .add("skipped", resultSet.getCountSkips())
                .add("elapsed", resultSet.getElapsedString());
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
        this.broadcastScenarioInfo(
                "test.start",
                engine,
                MsgArgs.of("trigger", trigger.getType().name())
        );
    }

    @Override
    public void onTestSkipped(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {
        this.broadcastScenarioWarn(
                "test.skip",
                engine,
                MsgArgs.of("condition", getConditionString(action))
        );
    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {
        switch (action.getType())
        {
            case ACTION_EXECUTE:
                this.broadcastScenarioInfo(
                        "test.action.run",
                        engine,
                        MsgArgs.of("action", ActionMetaUtils.getActionName(action.getAction().getExecutor()))
                );
                break;
            case ACTION_EXPECT:
                this.broadcastScenarioInfo(
                        "test.action.watch",
                        engine,
                        MsgArgs.of("action", ActionMetaUtils.getActionName(action.getAction().getExecutor()))
                );
                break;
            case CONDITION_REQUIRE:
                this.broadcastScenarioInfo(
                        "test.action.require.start",
                        engine,
                        MsgArgs.of("condition", getConditionString(action))
                );
                break;
        }
    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull ActionResult result)
    {
        this.broadcastScenarioSuccess(
                "test.action.run.success",
                engine,
                MsgArgs.of("action", result.getScenarioName())
        );
    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull ActionResult result)
    {
        this.broadcastScenarioSuccess(
                "test.action.watch.done",
                engine,
                MsgArgs.of("action", result.getScenarioName())
        );
    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction action, @NotNull Throwable error)
    {
        this.broadcastScenarioInfo(
                "test.action.run.fail",
                engine,
                MsgArgs.of("action", ActionMetaUtils.getActionName(action.getExecutor()))
                        .add("cause", error.getClass().getSimpleName() + ": " + error.getMessage())
        );
    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull ActionResult result, @NotNull CompiledAction expected)
    {
        this.broadcastScenarioWarn(
                "test.action.jumped",
                engine,
                MsgArgs.of("action", result.getScenarioName())
        );
    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {
        ScenarioFileStructure scenario = engine.getScenario();
        this.broadcastScenarioSuccess(
                "test.action.require.success",
                engine,
                MsgArgs.of("condition", getConditionString(action))
        );
    }

    @Override
    public void onConditionCheckFailed(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {
        this.broadcastScenarioError(
                "test.action.require.fail",
                engine,
                MsgArgs.of("condition", getConditionString(action))
        );
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
        ScenarioResultSet resultSet = new ScenarioResultSet(
                session.getStartedAt(),
                System.currentTimeMillis(),
                session.getScenarios()
        );

        this.terminals.forEach(t -> {
            if (resultSet.hasFailures() && this.maxAttempts <= 1)
                this.printAutoRetryTip(t);
            this.printSessionSummary(t, resultSet);
        });
    }

    protected void printSessionSummary(@NotNull Terminal terminal, ScenarioResultSet resultSet)
    {
        this.printSeparator(null, terminal, null, 50);

        terminal.writeLine("");

        if (!resultSet.hasFlakes())
            terminal.info(LangProvider.get("test.session.result.stats", getSummaryStatsMsgArgsBase(resultSet)));
        else
        {
            long flakeScenarios = /* flaking runs = */ resultSet.getCountFlakes();
            long attemptCount = resultSet.getFlakes().stream().mapToLong(ScenarioResult::getAttemptOf).sum();
            terminal.info(LangProvider.get(
                    "test.session.result.stats.with_flakes",
                    getSummaryStatsMsgArgsBase(resultSet)
                            .add("flakes", flakeScenarios)
                            .add("attempts", attemptCount)
            ));
        }


        terminal.writeLine("");

        String resultKey = null;
        String messageKey = null;
        if (resultSet.isAllPassed())
        {
            resultKey = "test.result.passed";
            messageKey = "test.session.result.message.passed";
        }
        else if (resultSet.hasFailures())
        {
            resultKey = "test.result.failed";
            messageKey = "test.session.result.message.failed";
        }
        else if (resultSet.isNothingTested())
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

            if (resultSet.isAllPassed())
                terminal.success(summary);
            else
                terminal.error(summary);

            terminal.writeLine("");
        }

        if (resultSet.hasFailures())
            this.printFailure(terminal, resultSet);

        this.printSeparator(null, terminal, null, 50);
    }

    private static String createFailureEntryMessage(ScenarioResult result)
    {
        String scenario = result.getScenario().getName();
        String cause = result.getCause().name();
        String action = (result.getFailedAction() == null) ? "???": ActionMetaUtils.getActionName(result.getFailedAction());
        return LangProvider.get(
                "test.session.result.failures.entry",
                MsgArgs.of("scenario", scenario)
                        .add("action", action)
                        .add("cause", cause)
        );

    }

    private static String createMultipleFailureEntryMessage(String baseMessage, long count)
    {
        if (count == 1)
            return baseMessage;

        return baseMessage + " (" + count + ")";
    }

    private void printFailure(Terminal terminal, ScenarioResultSet resultSet)
    {
        terminal.info(LangProvider.get(
                "test.session.result.failures",
                MsgArgs.of("count", resultSet.getCountFailures())
        ));
        List<String> distinctMessages = new HashMap<>(resultSet.getFailures().stream()
                // 失敗メッセージの基礎を作成
                .map(BukkitTestReporter::createFailureEntryMessage)
                // 重複をカウントして保持
                .collect(Collectors.groupingByConcurrent(msg -> msg, Collectors.counting())))
                .entrySet().stream()
                // メッセージを構築
                .map(e -> createMultipleFailureEntryMessage(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        distinctMessages.forEach(terminal::writeLine);
    }

    protected void printTestSummary(Terminal terminal, ScenarioFileStructure scenario, ScenarioResult result)
    {
        boolean passed = result.getCause() == ScenarioResultCause.PASSED;

        String resultKey;
        if (passed)
            resultKey = "test.result.passed";
        else if (result.getCause() == ScenarioResultCause.CANCELLED)
            resultKey = "test.result.cancelled";
        else if (result.getCause() == ScenarioResultCause.SKIPPED)
            resultKey = "test.result.skipped";
        else
            resultKey = "test.result.failed";
        String messageKey = "test.result.message." + result.getCause().name().toLowerCase();

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
        ScenarioResultCause cause = result.getCause();

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

        // 失敗したときのみ表示するため
        if (cause == ScenarioResultCause.PASSED || cause == ScenarioResultCause.CANCELLED || cause == ScenarioResultCause.SKIPPED)
            return;

        this.printActionTrace(terminal, scenario, result);

        if (result.getFailedAction() != null)
            terminal.info(this.withPrefix(result.getTestID(), scenario, LangProvider.get(
                    "test.result.detail.failed",
                    MsgArgs.of("action", result.getFailedAction().getClass().getSimpleName())
            )));
    }

    protected void printActionTrace(Terminal terminal, ScenarioFileStructure scenario, ScenarioResult result)
    {
        terminal.info(this.withPrefix(result.getTestID(), scenario, LangProvider.get(
                "test.result.actions",
                MsgArgs.of("count", result.getActionResults().size())
        )));

        result.getActionResults().forEach(actionResult -> this.printActionDetail(terminal, scenario, result, actionResult));
    }

    protected void printActionDetail(Terminal terminal, ScenarioFileStructure scenario, ScenarioResult scenarioResult, ActionResult actionResult)
    {
        String actionName = actionResult.getScenarioName();
        String key;
        if (actionResult.isSkipped())
            key = "test.result.actions.skip";
        else if (actionResult.isSuccess())
            key = "test.result.actions.success";
        else
            key = "test.result.actions.fail";

        MsgArgs args = MsgArgs.of("actionName", actionName)
                .add("scenarioType", actionResult.getScenarioType().getKey())
                .add("runID", actionResult.getRunID().toString().substring(0, 8));

        if (!actionResult.isFailed())
        {
            terminal.info(this.withPrefix(scenarioResult.getTestID(), scenario, LangProvider.get(key, args)));
            return;
        }

        String causeMessage = "";
        if (actionResult.getCause() == ActionResultCause.UNRESOLVED_REFERENCES)
            causeMessage = "Unresolved references: {" + String.join(", ", actionResult.getUnresolvedReferences()) + "} ";
        else if (actionResult.getError() != null)
        {
            Throwable error = actionResult.getError();
            if (error instanceof IllegalActionInputException)
            {
                IllegalActionInputException e = (IllegalActionInputException) error;
                causeMessage = "Illegal action input '" + e.getToken().getName() + "': " + e.getMessage();
            }
            else
                causeMessage = "Caused by " + error.getClass().getSimpleName() + " - " + error.getMessage();
        }

        args.add("cause", actionResult.getCause().name())
                .add("message", causeMessage);

        terminal.info(this.withPrefix(scenarioResult.getTestID(), scenario, LangProvider.get(key, args)));
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
        terminal.hint(LangProvider.get("test.session.result.retry_tip"));
    }
}
