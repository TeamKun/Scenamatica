package org.kunlab.scenamatica.scenario.engine;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResultDeliverer;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenario.ScenarioResultDelivererImpl;
import org.kunlab.scenamatica.scenario.ScenarioResultImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class ScenarioExecutor
{
    private final ScenamaticaRegistry registry;
    private final TestReporter testReporter;
    private final ScenarioEngineImpl engine;
    private final List<? extends CompiledScenarioAction> actions;
    private final List<? extends CompiledTriggerAction> triggerActions;
    private final CompiledScenarioAction runIf;
    private final ScenarioFileStructure scenario;
    private final ActionRunManager actionManager;
    private final ScenarioActionListener listener;
    private final ScenarioResultDeliverer deliverer;
    private final List<CompiledScenarioAction> watchedActions;
    private final Plugin plugin;
    private final UUID testID;
    private final long startedAt;
    private final String logPrefix;
    private final int attemptedCount;

    private ScenarioState state;
    private long elapsedTicks;
    private CompiledScenarioAction currentScenario;

    public ScenarioExecutor(ScenarioEngineImpl engine,
                            ActionRunManager actionManager,
                            ScenarioActionListener listener,
                            List<? extends CompiledScenarioAction> actions,
                            List<? extends CompiledTriggerAction> triggerActions,
                            CompiledScenarioAction runIf,
                            int attemptedCount)
    {
        this.engine = engine;
        this.actionManager = actionManager;
        this.listener = listener;
        this.actions = actions;
        this.triggerActions = triggerActions;
        this.runIf = runIf;
        this.attemptedCount = attemptedCount;

        this.registry = engine.getRegistry();
        this.plugin = engine.getPlugin();
        this.testReporter = engine.getTestReporter();
        this.scenario = engine.getScenario();

        this.watchedActions = new ArrayList<>();
        this.testID = UUID.randomUUID();
        this.startedAt = System.currentTimeMillis();
        this.logPrefix = LogUtils.gerScenarioPrefix(this.testID, this.scenario);

        this.deliverer = new ScenarioResultDelivererImpl(
                this.registry,
                this.scenario,
                this.testID,
                this.startedAt,
                this.attemptedCount
        );

        this.state = ScenarioState.STAND_BY;
        this.elapsedTicks = 0;
        this.currentScenario = null;
    }

    public ScenarioResult start(@NotNull TriggerStructure trigger) throws TriggerNotFoundException
    {
        ScenarioResult result = this.start$1(trigger);
        this.actionManager.getWatcherManager().unregisterWatchers(this.plugin, WatchType.SCENARIO);
        return result;
    }

    private ScenarioResult start$1(@NotNull TriggerStructure trigger) throws TriggerNotFoundException
    {

        CompiledTriggerAction compiledTrigger = this.findTriggerOrThrow(trigger);

        ScenarioResult mayResult = this.testRunConditionIfExists(this.runIf);
        if (mayResult != null)
            return mayResult;

        mayResult = this.testRunConditionIfExists(compiledTrigger.getRunIf());
        if (mayResult != null)
            return mayResult;

        mayResult = this.runBeforeIfPresent(compiledTrigger);
        if (mayResult != null)
            return mayResult;

        this.state = ScenarioState.RUNNING_MAIN;
        this.infoWithPrefixIfVerbose(
                LangProvider.get(
                        "scenario.run.starting.main",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                )
        );
        mayResult = this.runScenario(this.actions);
        if (mayResult != null)
            return mayResult;

        mayResult = this.runAfterIfPresent(compiledTrigger);
        if (mayResult != null)
            return mayResult;

        this.state = ScenarioState.FINISHED;
        // genResult は, state を参照するので以下と順番を変えると最終的な state がおかしくなる。

        return this.genResult(ScenarioResultCause.PASSED);
    }

    public void cancel()
    {
        this.deliverer.kill();
    }

    public void onTick()
    {
        this.elapsedTicks++;

        this.getDeliverer().onTick();

        if (!this.isTimedOut())
            return;

        // タイムアウト処理。
        if (this.deliverer.isWaiting())
            this.deliverer.setResult(this.genResult(ScenarioResultCause.RUN_TIMED_OUT));
        else
            this.engine.cancel();
    }

    private boolean isTimedOut()
    {
        return this.scenario.getTimeout() != -1 && this.getElapsedTicks() >= this.scenario.getTimeout();
    }

    public CompiledTriggerAction findTriggerOrThrow(TriggerStructure trigger) throws TriggerNotFoundException
    {
        return this.triggerActions.parallelStream()
                .filter(t -> t.getTrigger().getType() == trigger.getType())
                .filter(t -> Objects.equals(t.getTrigger().getArgument(), trigger.getArgument()))
                .findFirst().orElseThrow(() ->
                        new TriggerNotFoundException(this.scenario.getName(), trigger.getType())
                );
    }

    private ScenarioResult testRunConditionIfExists(@Nullable CompiledScenarioAction runIf)
    {
        if (runIf != null)
        {
            ScenarioResult conditionResult = this.testCondition(runIf);
            // コンディションチェックに失敗した(満たしていない)場合は(エラーにはせずに)スキップする。
            if (conditionResult.getScenarioResultCause() != ScenarioResultCause.PASSED)
            {
                this.testReporter.onTestSkipped(this.engine, runIf);

                return this.genResult(ScenarioResultCause.SKIPPED); // 実質的にはエラーではないので、スキップする。
            }
        }

        return null;
    }

    @Nullable
    private ScenarioResult runScenario(List<? extends CompiledScenarioAction> scenario)
    {
        // 飛び判定用に, 予めすべてのアクションを監視対象にしておく。
        List<CompiledAction> watches = scenario.stream()
                .filter(a -> a.getType() == ScenarioType.ACTION_EXPECT)
                .map(CompiledScenarioAction::getAction)
                .collect(Collectors.toList());
        this.actionManager.getWatcherManager().registerWatchers(
                this.plugin,
                this.engine,
                this.scenario,
                watches,
                WatchType.SCENARIO
        );

        for (int i = 0; i < scenario.size(); i++)
        {
            if (!this.engine.isRunning())
                if (this.isTimedOut())
                    return this.genResult(ScenarioResultCause.RUN_TIMED_OUT);
                else
                    return this.genResult(ScenarioResultCause.CANCELLED);

            CompiledScenarioAction scenarioStructure = scenario.get(i);
            CompiledScenarioAction next = i + 1 < scenario.size() ? scenario.get(i + 1): null;

            ScenarioResult result = this.runScenario(scenarioStructure, next);
            if (!(result.getScenarioResultCause() == ScenarioResultCause.PASSED || result.getScenarioResultCause() == ScenarioResultCause.SKIPPED))
                return result;
        }

        return null;
    }

    private ScenarioResult runScenario(CompiledScenarioAction scenario, CompiledScenarioAction next)
    {
        if (scenario.getRunIf() != null)
        {
            ScenarioResult result = this.testCondition(scenario.getRunIf());
            if (result.getScenarioResultCause() != ScenarioResultCause.PASSED)
                return this.genResult(ScenarioResultCause.SKIPPED);
        }

        this.currentScenario = scenario;
        ScenarioType type = scenario.getType();

        switch (type)
        {
            case ACTION_EXECUTE:
                this.doAction(scenario, next);
                break;
            case ACTION_EXPECT:
                this.addWatch(scenario);
                break;
            case CONDITION_REQUIRE:
                return this.testCondition(scenario);
        }

        return this.deliverer.waitResult(scenario.getStructure().getTimeout(), this.state);
    }

    private void doAction(CompiledScenarioAction scenario, CompiledScenarioAction next)
    {
        this.testReporter.onActionStart(this.engine, scenario);

        // このアクションにより, 次のアクションが起きるかもしれないので、次が EXPECT なら監視対象にする。
        if (next != null && next.getType() == ScenarioType.ACTION_EXPECT)
            this.addWatch(next);

        this.actionManager.queueExecute(scenario.getAction());
    }

    private ScenarioResult testCondition(CompiledScenarioAction scenario)
    {
        this.testReporter.onActionStart(this.engine, scenario);

        assert scenario.getAction().getExecutor() instanceof Requireable;
        Requireable requireable = (Requireable) scenario.getAction().getExecutor();

        boolean result;
        try
        {
            result = requireable.isConditionFulfilled(scenario.getAction().getArgument(), this.engine);
        }
        catch (Throwable e)
        {
            this.registry.getExceptionHandler().report(e);
            this.testReporter.onConditionCheckFailed(this.engine, scenario);
            return this.genResult(ScenarioResultCause.INTERNAL_ERROR);
        }

        ScenarioResult scenarioResult;
        if (result)
        {
            this.testReporter.onConditionCheckSuccess(this.engine, scenario);
            scenarioResult = this.genResult(ScenarioResultCause.PASSED);
        }
        else
        {
            this.testReporter.onConditionCheckFailed(this.engine, scenario);
            scenarioResult = this.genResult(ScenarioResultCause.ILLEGAL_CONDITION);
        }

        return scenarioResult;
    }

    private void addWatch(CompiledScenarioAction/*<?>*/ scenario)
    {
        if (this.watchedActions.contains(scenario))
            return;

        assert scenario.getAction().getExecutor() instanceof Watchable;
        Watchable requireable = (Watchable) scenario.getAction().getExecutor();

        this.watchedActions.add(scenario);

        this.testReporter.onActionStart(this.engine, scenario);
        requireable.onStartWatching(scenario.getAction().getArgument(), this.plugin, null);
        this.listener.setWaitingFor(scenario);
    }

    private ScenarioResult runBeforeIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getBeforeThat().isEmpty())
        {
            this.state = ScenarioState.RUNNING_BEFORE;
            this.infoWithPrefixIfVerbose(LangProvider.get(
                            "scenario.run.starting.before",
                            MsgArgs.of("scenarioName", this.scenario.getName())
                    )
            );
            return this.runScenario(trigger.getBeforeActions());
        }

        return null;
    }

    private ScenarioResult runAfterIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getAfterThat().isEmpty())
        {
            this.state = ScenarioState.RUNNING_AFTER;
            this.infoWithPrefixIfVerbose(LangProvider.get(
                            "scenario.run.starting.after",
                            MsgArgs.of("scenarioName", this.scenario.getName())
                    )
            );
            return this.runScenario(trigger.getAfterActions());
        }

        return null;
    }

    private void infoWithPrefixIfVerbose(String message)
    {
        if (!this.registry.getEnvironment().isVerbose())
            return;
        this.registry.getLogger().log(Level.INFO, this.logPrefix + message);
    }

    private ScenarioResult genResult(ScenarioResultCause cause)
    {
        return new ScenarioResultImpl(
                this.scenario,
                this.testID,
                this.state,
                cause,
                this.startedAt,
                this.attemptedCount
        );
    }
}
