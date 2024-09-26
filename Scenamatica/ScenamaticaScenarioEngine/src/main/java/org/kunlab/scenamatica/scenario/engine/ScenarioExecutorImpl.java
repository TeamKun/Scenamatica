package org.kunlab.scenamatica.scenario.engine;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.enums.ActionResultCause;
import org.kunlab.scenamatica.enums.RunAs;
import org.kunlab.scenamatica.enums.RunOn;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ActionResultDeliverer;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioExecutor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenario.ActionResultDelivererImpl;
import org.kunlab.scenamatica.scenario.ScenarioResultImpl;
import org.kunlab.scenamatica.scenario.ScenarioWaitTimedOutException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class ScenarioExecutorImpl implements ScenarioExecutor
{
    private static final String STORAGE_KEY_PREFIX = "scenario";
    private static final String STORAGE_KEY_OUTPUT = "output";

    private final ScenamaticaRegistry registry;
    private final TestReporter testReporter;
    private final ScenarioEngineImpl engine;
    private final List<? extends CompiledScenarioAction> actions;
    private final List<? extends CompiledTriggerAction> triggerActions;
    private final CompiledScenarioAction runIf;
    private final SessionStorage variable;
    private final ScenarioFileStructure scenario;
    private final ActionRunManager actionManager;
    private final ScenarioActionListener listener;
    private final ActionResultDeliverer deliverer;
    private final List<CompiledScenarioAction> watchedActions;
    private final Plugin plugin;
    private final UUID testID;
    private final long startedAt;
    private final String logPrefix;
    private final int attemptedCount;

    private ScenarioState state;
    private long elapsedTicks;
    private CompiledScenarioAction currentScenario;

    public ScenarioExecutorImpl(ScenarioEngineImpl engine,
                                ActionRunManager actionManager,
                                ScenarioActionListener listener,
                                List<? extends CompiledScenarioAction> actions,
                                List<? extends CompiledTriggerAction> triggerActions,
                                CompiledScenarioAction runIf,
                                SessionStorage variable,
                                int attemptedCount)
    {
        this.engine = engine;
        this.actionManager = actionManager;
        this.listener = listener;
        this.actions = actions;
        this.triggerActions = triggerActions;
        this.runIf = runIf;
        this.variable = variable;
        this.attemptedCount = attemptedCount;

        this.registry = engine.getRegistry();
        this.plugin = engine.getPlugin();
        this.testReporter = engine.getTestReporter();
        this.scenario = engine.getScenario();

        this.watchedActions = new ArrayList<>();
        this.testID = UUID.randomUUID();
        this.startedAt = System.currentTimeMillis();
        this.logPrefix = LogUtils.gerScenarioPrefix(this.testID, this.scenario);

        this.deliverer = new ActionResultDelivererImpl();

        this.state = ScenarioState.STAND_BY;
        this.elapsedTicks = 0;
        this.currentScenario = null;
    }

    private static ScenarioResultCause toScenarioScopeCase(ActionResultCause actionCause)
    {
        switch (actionCause)
        {
            case EXECUTION_FAILED:
                return ScenarioResultCause.ACTION_EXECUTION_FAILED;
            case EXECUTION_JUMPED:
                return ScenarioResultCause.ACTION_EXPECTATION_JUMPED;
            case UNEXPECTED_CONDITION:
                return ScenarioResultCause.ILLEGAL_CONDITION;
            case INTERNAL_ERROR:
            case UNRESOLVED_REFERENCES:
                return ScenarioResultCause.INTERNAL_ERROR;
            case TIMED_OUT:
                return ScenarioResultCause.SCENARIO_TIMED_OUT;
            case SKIPPED:
                return ScenarioResultCause.SKIPPED;
            default:
                throw new IllegalStateException("Unexpected value: " + actionCause);
        }
    }

    private static boolean isStorageScenarioNameNeeded(RunOn runOn, RunAs runAs)
    {
        return runOn == RunOn.SCENARIOS || (runOn == RunOn.TRIGGER && runAs != RunAs.RUNIF);
    }

    private static String genStorageKey(@NotNull RunOn runOn, @NotNull RunAs runAs)
    {
        StringBuilder key = new StringBuilder(STORAGE_KEY_PREFIX)
                .append(".").append(runOn.getKey());

        if (isStorageScenarioNameNeeded(runOn, runAs))
            key.append(".%s");  // 参照名が入る。

        if (!(runAs == RunAs.NORMAL || runAs == RunAs.RUNIF))
            key.append(".").append(runAs.getKey());

        return key.append(".").append(STORAGE_KEY_OUTPUT).toString();

        /*
         1. runOn == TRIGGER
            1.1. runAs == NORMAL => ERR
            1.2. runAs == RUNIF
                scenario.trigger.runif.output
            1.3. runAs == BEFORE
                scenario.trigger.before.<idx|name>.output
            1.4. runAs == AFTER
                scenario.trigger.after.<idx|name>.output
        2. runOn == SCENARIOS
            2.1. runAs == NORMAL
                scenario.scenarios.<idx|name>.output
            2.2. runAs == RUNIF
                scenario.scenarios.<idx|name>.runif.output
            2.3. runAs == BEFORE => ERR
            2.4. runAs == AFTER => ERR
        3. runOn == RUNIF
            3.1. runAs == NORMAL => ERR
            3.2. runAs == RUNIF
                scenario.runif.output
            3.3. runAs == BEFORE => ERR
            3.4. runAs == AFTER => ERR
         */
    }

    public ScenarioResult start(@NotNull TriggerStructure trigger) throws TriggerNotFoundException
    {
        ScenarioResult result = this.start$1(trigger);

        // 後始末
        this.actionManager.getWatcherManager().unregisterWatchers(this.plugin, WatchType.SCENARIO);
        return result;
    }

    private ScenarioResult start$1(@NotNull TriggerStructure trigger) throws TriggerNotFoundException
    {
        CompiledTriggerAction compiledTrigger = this.findTriggerOrThrow(trigger);

        if (this.runIf != null)
        {
            ActionResult result = this.runExecuteCondition(this.runIf, RunOn.RUNIF); // シナリオレベルの runif
            if (!result.isSuccess())
                return this.genResult(ScenarioResultCause.SKIPPED);
        }

        if (compiledTrigger.getRunIf() != null)
        {
            ActionResult result = this.runExecuteCondition(compiledTrigger.getRunIf(), RunOn.TRIGGER);  // トリガーレベルの runif
            if (!result.isSuccess())
                return this.genResult(ScenarioResultCause.SKIPPED);
        }

        if (!trigger.getBeforeThat().isEmpty())
        {
            ScenarioResult result = this.runBefore(compiledTrigger);
            if (!result.getCause().isOK())
                return result;
        }


        this.state = ScenarioState.RUNNING_MAIN;
        this.infoWithPrefixIfVerbose(
                LangProvider.get(
                        "scenario.run.starting.main",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                )
        );
        ScenarioResult result = this.runScenario(this.actions, RunAs.NORMAL);
        if (!result.getCause().isOK())
            return result;

        if (!trigger.getAfterThat().isEmpty())
        {
            ScenarioResult afterResult = this.runAfter(compiledTrigger);
            if (!afterResult.getCause().isOK())
                return afterResult;
        }

        this.state = ScenarioState.FINISHED;
        // genResult は, state を参照するので以下と順番を変えると最終的な state がおかしくなる。

        return result;
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
            this.deliverer.timedout();
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

    @NotNull
    private ActionResult runExecuteCondition(@NotNull CompiledScenarioAction runIf, RunOn on)
    {
        ActionResult result = this.testCondition(runIf);
        // コンディションチェックに失敗した(満たしていない)場合は(エラーにはせずに)スキップする。
        if (!result.isSuccess())
            this.testReporter.onTestSkipped(this.engine, runIf);
        return result;
    }

    @NotNull
    private ScenarioResult runScenario(List<? extends CompiledScenarioAction> scenario, RunAs as)
    {
        // 飛び判定用に, 予めすべてのアクションを監視対象にしておく。
        List<CompiledAction> watches = scenario.stream()
                .filter(a -> a.getType() == ScenarioType.ACTION_EXPECT)
                .map(CompiledScenarioAction::getAction)
                .collect(Collectors.toList());
        this.actionManager.getWatcherManager().registerWatchers(
                this.engine,
                this.scenario,
                watches,
                WatchType.SCENARIO
        );

        boolean allSuccess = true;
        ActionResultCause cause = null;
        List<ActionResult> results;
        try
        {
            results = new ArrayList<>();
            for (int i = 0; i < scenario.size(); i++)
            {
                if (!this.engine.isRunning())
                    if (this.isTimedOut())
                        return this.genResult(ScenarioResultCause.RUN_TIMED_OUT, results);
                    else
                        return this.genResult(ScenarioResultCause.CANCELLED, results);

                CompiledScenarioAction scenarioStructure = scenario.get(i);
                CompiledScenarioAction next = i + 1 < scenario.size() ? scenario.get(i + 1): null;

                ActionResult result = this.runScenario(scenarioStructure, next);
                if (result == null)
                    continue; // スキップされた場合は次のアクションへ。

                results.add(result);

                if (!result.isSuccess())
                {
                    allSuccess = false;
                    cause = result.getCause();
                }

                if (result.isHalt())
                    break;  // Halt された場合は即座に終了。
            }
        }
        catch (ScenarioWaitTimedOutException ignored)
        {
            return this.genResult(ScenarioResultCause.SCENARIO_TIMED_OUT);
        }
        catch (Throwable e)
        {
            this.registry.getExceptionHandler().report(e);
            return this.genResult(ScenarioResultCause.INTERNAL_ERROR);
        }

        if (allSuccess)
            return this.genResult(ScenarioResultCause.PASSED, results);
        else if (cause != null)
            return this.genResult(toScenarioScopeCase(cause), results);
        else
            return this.genResult(ScenarioResultCause.INTERNAL_ERROR, results);
    }

    @Override
    public void resolveInputs(CompiledAction action)
    {
        InputBoard input = action.getContext().getInput();
        if (input.hasUnresolvedReferences())
            input.resolveReferences(this.registry.getScenarioFileManager().getSerializer(), this.variable);
    }

    private ActionResult runScenario(CompiledScenarioAction scenario, CompiledScenarioAction next)
    {
        if (scenario.getRunIf() != null)
        {
            CompiledScenarioAction runIf = scenario.getRunIf();
            ActionResult result = this.runExecuteCondition(runIf, RunOn.SCENARIOS); // シナリオレベルの runif
            if (!result.isSuccess())
            {
                ActionContext context = scenario.getAction().getContext();
                context.skip(); // スキップとしてマークしておく。
                return null;
            }
        }

        ActionContext context = scenario.getAction().getContext();
        try
        {
            this.resolveInputs(scenario.getAction());
        }
        catch (BrokenReferenceException e)
        {
            context.fail(ActionResultCause.UNRESOLVED_REFERENCES, e);
            return context.createResult(scenario.getAction());
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

        try
        {
            return this.deliverer.waitForResult(scenario.getStructure().getTimeout(), this.state);
        }
        catch (ScenarioWaitTimedOutException e)
        {
            context.fail(ActionResultCause.TIMED_OUT);
            return context.createResult(scenario.getAction());
        }
        catch (BrokenReferenceException e)
        {
            context.fail(ActionResultCause.UNRESOLVED_REFERENCES, e);
            return context.createResult(scenario.getAction());
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            context.fail(ActionResultCause.INTERNAL_ERROR);
            return context.createResult(scenario.getAction());
        }
    }

    private void doAction(CompiledScenarioAction scenario, CompiledScenarioAction next)
    {
        this.testReporter.onActionStart(this.engine, scenario);

        // このアクションにより, 次のアクションが起きるかもしれないので、次が EXPECT なら監視対象にする。
        if (next != null && next.getType() == ScenarioType.ACTION_EXPECT)
            this.addWatch(next);

        this.actionManager.queueExecute(scenario.getAction());
    }

    private ActionResult testCondition(CompiledScenarioAction scenario)
    {
        this.testReporter.onActionStart(this.engine, scenario);

        assert scenario.getAction().getExecutor() instanceof Requireable;
        Requireable requireable = (Requireable) scenario.getAction().getExecutor();

        ActionContext context = scenario.getAction().getContext();
        try
        {
            boolean result = requireable.checkConditionFulfilled(context);

            if (result && context.isSuccess())  // 正規化： 成功状態がない場合は, 暗黙的に成功とする。
                context.success();
            else
                context.fail(ActionResultCause.UNEXPECTED_CONDITION);
        }
        catch (BrokenReferenceException e)
        {
            context.fail(ActionResultCause.UNRESOLVED_REFERENCES, e);
        }
        catch (Throwable e)
        {
            context.fail(e);
            this.registry.getExceptionHandler().report(e);
        }

        if (context.isSuccess())
            this.testReporter.onConditionCheckSuccess(this.engine, scenario);
        else
            this.testReporter.onConditionCheckFailed(this.engine, scenario);

        return context.createResult(scenario.getAction());
    }

    private void addWatch(CompiledScenarioAction/*<?>*/ scenario)
    {
        if (this.watchedActions.contains(scenario))
            return;

        assert scenario.getAction().getExecutor() instanceof Expectable;
        Expectable requireable = (Expectable) scenario.getAction().getExecutor();

        this.watchedActions.add(scenario);

        this.testReporter.onActionStart(this.engine, scenario);
        requireable.onStartWatching(scenario.getAction().getContext(), null);
        this.listener.setWaitingFor(scenario);
    }

    private ScenarioResult runBefore(CompiledTriggerAction trigger)
    {
        this.state = ScenarioState.RUNNING_BEFORE;
        this.infoWithPrefixIfVerbose(LangProvider.get(
                        "scenario.run.starting.before",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                )
        );
        return this.runScenario(trigger.getBeforeActions(), RunAs.BEFORE);
    }

    private ScenarioResult runAfter(CompiledTriggerAction trigger)
    {
        this.state = ScenarioState.RUNNING_AFTER;
        this.infoWithPrefixIfVerbose(LangProvider.get(
                        "scenario.run.starting.after",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                )
        );
        return this.runScenario(trigger.getAfterActions(), RunAs.AFTER);
    }

    private void infoWithPrefixIfVerbose(String message)
    {
        if (!this.registry.getEnvironment().isVerbose())
            return;
        this.registry.getLogger().log(Level.INFO, this.logPrefix + message);
    }

    private ScenarioResult genResult(ScenarioResultCause cause, List<ActionResult> actionResults)
    {
        return new ScenarioResultImpl(
                this.scenario,
                this.testID,
                this.state,
                cause,
                actionResults,
                this.startedAt,
                this.attemptedCount
        );
    }

    private ScenarioResult genResult(ScenarioResultCause cause)
    {
        return this.genResult(cause, Collections.emptyList());
    }

    @Override
    public void uploadScenarioOutputs(ActionContext sender, Map<String, Object> outputs)
    {
        int idx = this.getIndex(sender);
        this.makeOutput(sender.getRunOn(), sender.getRunAs(), idx, sender.getScenarioName(), outputs);
    }

    private int getIndex(ActionContext sender)
    {
        Predicate<? super CompiledScenarioAction> predicate;
        if (sender.getRunAs() == RunAs.RUNIF)
            predicate = a -> a.getRunIf() != null && a.getRunIf().getAction().getContext().getContextID().equals(sender.getContextID());
        else
            predicate = a -> a.getAction().getContext().getContextID().equals(sender.getContextID());


        switch (sender.getRunOn())
        {
            case SCENARIOS:
                return this.getIndexForScenario(predicate);
            case TRIGGER:
                return this.getIndexForTrigger(sender, predicate);
            default:
                return -1;
        }
    }

    private int getIndexForScenario(Predicate<? super CompiledScenarioAction> predicate)
    {
        IllegalStateException e = new IllegalStateException("Unable to upload outputs: Unrecognized sender.");

        return this.actions.stream()
                .filter(predicate)
                .map(this.actions::indexOf)
                .findFirst()
                .orElseThrow(() -> e);
    }

    private int getIndexForTrigger(ActionContext sender, Predicate<? super CompiledScenarioAction> predicate)
    {
        IllegalStateException e = new IllegalStateException("Unable to upload outputs: Unrecognized sender.");

        RunAs runAs = sender.getRunAs();
        List<CompiledScenarioAction> population;

        switch (runAs)
        {
            case BEFORE:
                population = this.getBeforeActionsFromTriggers();
                break;
            case AFTER:
                population = this.getAfterActionsFromTriggers();
                break;
            default:
                return -1;
        }

        return population.stream()
                .filter(predicate)
                .map(population::indexOf)
                .findFirst()
                .orElseThrow(() -> e);
    }

    private List<CompiledScenarioAction> getBeforeActionsFromTriggers()
    {
        return this.triggerActions.stream()
                .map(CompiledTriggerAction::getBeforeActions)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<CompiledScenarioAction> getAfterActionsFromTriggers()
    {
        return this.triggerActions.stream()
                .map(CompiledTriggerAction::getAfterActions)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private void makeOutput(RunOn runOn, RunAs runAs, int idx, String scenarioName, Map<String, Object> outputs)
    {
        Map<String, Object> copy = new HashMap<>(outputs);

        String key = genStorageKey(runOn, runAs);
        if (idx != -1)
            this.variable.set(key.replace("%s", String.valueOf(idx)), copy);
        if (scenarioName != null)
            this.variable.set(key.replace("%s", scenarioName), copy);
    }
}
