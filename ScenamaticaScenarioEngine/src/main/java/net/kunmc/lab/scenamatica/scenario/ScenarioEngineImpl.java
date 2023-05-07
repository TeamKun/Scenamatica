package net.kunmc.lab.scenamatica.scenario;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.scenamatica.commons.utils.LogUtils;
import net.kunmc.lab.scenamatica.commons.utils.ThreadingUtil;
import net.kunmc.lab.scenamatica.enums.MilestoneScope;
import net.kunmc.lab.scenamatica.enums.ScenarioResultCause;
import net.kunmc.lab.scenamatica.enums.ScenarioState;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResultDeliverer;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class ScenarioEngineImpl implements ScenarioEngine
{
    private final ScenamaticaRegistry registry;
    private final ScenarioManager manager;
    private final ActionManager actionManager;
    private final TestReporter testReporter;
    private final Plugin plugin;
    private final ScenarioFileBean scenario;
    private final ScenarioCompiler compiler;
    private final ScenarioActionListener listener;
    private final List<CompiledScenarioAction<?>> actions;
    private final List<CompiledTriggerAction> triggerActions;
    private final CompiledScenarioAction<?> runIf;

    private boolean isRunning;
    private long elapsedTicks;
    private TriggerBean ranBy;
    private UUID testID;
    private long startedAt;
    private String logPrefix;
    private boolean isAutoRun;
    private ScenarioState state;
    private CompiledScenarioAction<?> currentScenario;
    private ScenarioResultDeliverer deliverer;
    private List<CompiledScenarioAction<?>> watchedActions;  // 監視対象になったアクション

    public ScenarioEngineImpl(@NotNull ScenamaticaRegistry registry,
                              @NotNull ScenarioManager manager,
                              @NotNull ActionManager actionManager,
                              @NotNull TestReporter testReporter,
                              @NotNull Plugin plugin,
                              @NotNull ScenarioFileBean scenario)
    {
        this.registry = registry;
        this.manager = manager;
        this.actionManager = actionManager;
        this.testReporter = testReporter;
        this.plugin = plugin;
        this.scenario = scenario;
        this.listener = new ScenarioActionListenerImpl(this, registry);
        this.compiler = new ScenarioCompiler(this, registry, actionManager);
        this.state = ScenarioState.STAND_BY;

        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);

        // 以下、 アクションをコンパイルしてキャッシュしておく。
        this.compiler.notifyCompileStart();

        // 本シナリオのコンパイル
        this.actions = this.compiler.compileMain(this.scenario.getScenario());

        // トリガの before/after のコンパイル
        this.triggerActions = this.compiler.compileTriggerActions(scenario.getTriggers());

        // runIf のコンパイル
        if (scenario.getRunIf() != null)
            this.runIf = this.compiler.compileRunIf(scenario.getRunIf());
        else
            this.runIf = null;

        // トリガのコンパイル
        this.registry.getTriggerManager().bakeTriggers(this);
    }

    @Override
    @NotNull
    public ScenarioResult start(@NotNull TriggerBean trigger) throws TriggerNotFoundException
    {
        this.setRunInfo(trigger);
        this.testReporter.onTestStart(this, trigger);
        CompiledTriggerAction compiledTrigger = this.findTriggerOrThrow(trigger);

        // あとかたづけ は、できるだけ明瞭にしたいのでこのメソッド内で完結する。

        Context context = this.prepareContext();  // State 変更: CONTEXT_PREPARING
        if (context == null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry, this::cleanUp);
            this.genResult(ScenarioResultCause.CONTEXT_PREPARATION_FAILED);
        }

        this.logWithPrefix(Level.INFO, LangProvider.get(
                "scenario.run.engine.starting",
                MsgArgs.of("scenarioName", this.scenario.getName())
        ));
        this.state = ScenarioState.STARTING;

        return this.startScenarioRun(compiledTrigger);
    }

    private ScenarioResult startScenarioRun(@NotNull CompiledTriggerAction compiledTrigger)
    {
        ScenarioResult mayResult = this.testRunConditionIfExists(this.runIf);
        if (mayResult != null)
            return mayResult;  // あとかたづけは上に包含されている。

        mayResult = this.testRunConditionIfExists(compiledTrigger.getRunIf());
        if (mayResult != null)
            return mayResult;  // あとかたづけは上に包含されている。

        mayResult = this.runBeforeIfPresent(compiledTrigger);
        if (mayResult != null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry, this::cleanUp);
            return mayResult;
        }

        this.state = ScenarioState.RUNNING_MAIN;
        this.logWithPrefix(Level.INFO, LangProvider.get(

                "scenario.run.starting.main",
                MsgArgs.of("scenarioName", this.scenario.getName())
        ));
        mayResult = this.runScenario(this.actions);
        if (mayResult != null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry, this::cleanUp);
            return mayResult;
        }

        mayResult = this.runAfterIfPresent(compiledTrigger);
        if (mayResult != null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry, this::cleanUp);
            return mayResult;
        }

        this.state = ScenarioState.FINISHED;
        ScenarioResult result = this.genResult(ScenarioResultCause.PASSED);
        // genResult は, state を参照するので以下と順番を変えると最終的な state がおかしくなる。

        // あとかたづけ
        ThreadingUtil.waitFor(this.registry, this::cleanUp);

        return result;
    }

    private ScenarioResult testRunConditionIfExists(@Nullable CompiledScenarioAction<?> runIf)
    {
        if (runIf != null)
        {
            ScenarioResult conditionResult = this.testCondition(runIf);
            // コンディションチェックに失敗した(満たしていない)場合は(エラーにはせずに)スキップする。
            if (conditionResult.getScenarioResultCause() != ScenarioResultCause.PASSED)
            {
                this.testReporter.onTestSkipped(this, runIf);

                // あとかたづけ
                ThreadingUtil.waitFor(this.registry, this::cleanUp);
                return this.genResult(ScenarioResultCause.SKIPPED); // 実質的にはエラーではないので、スキップする。
            }
        }

        return null;
    }

    @NotNull
    private CompiledTriggerAction findTriggerOrThrow(TriggerBean trigger) throws TriggerNotFoundException
    {
        return this.triggerActions.parallelStream()
                .filter(t -> t.getTrigger().getType() == trigger.getType())
                .filter(t -> Objects.equals(t.getTrigger().getArgument(), trigger.getArgument()))
                .findFirst().orElseThrow(() -> new TriggerNotFoundException(trigger.getType()));
    }

    @Nullable
    private Context prepareContext()
    {
        this.state = ScenarioState.CONTEXT_PREPARING;

        try
        {
            return this.registry.getContextManager().prepareContext(this.scenario, this.testID);
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            this.logWithPrefix(Level.WARNING, LangProvider.get(
                    "scenario.run.prepare.fail",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));

            return null;
        }
    }

    private void cleanUp()
    {
        this.state = ScenarioState.CLEANING_UP;
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);
        // シナリオのアクションの監視を全て解除しておく。
        this.actionManager.getWatcherManager().unregisterWatchers(this.plugin, WatchType.SCENARIO);
        this.logWithPrefix(Level.INFO, LangProvider.get(
                "scenario.run.prepare.destroy",
                MsgArgs.of("scenarioName", this.scenario.getName())
        ));
        this.registry.getContextManager().destroyContext();

        this.state = ScenarioState.STAND_BY;
        this.isRunning = false;  // これの位置を変えると, 排他の問題でバグる
    }

    private ScenarioResult runBeforeIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getBeforeThat().isEmpty())
        {
            this.state = ScenarioState.RUNNING_BEFORE;
            this.logWithPrefix(Level.INFO, LangProvider.get(
                    "scenario.run.starting.before",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));
            return this.runScenario(trigger.getBeforeActions());
        }

        return null;
    }

    private ScenarioResult runAfterIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getAfterThat().isEmpty())
        {
            this.state = ScenarioState.RUNNING_AFTER;
            this.logWithPrefix(Level.INFO, LangProvider.get(
                    "scenario.run.starting.after",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));
            return this.runScenario(trigger.getAfterActions());
        }

        return null;
    }

    @Nullable
    private ScenarioResult runScenario(List<? extends CompiledScenarioAction<?>> scenario)
    {
        // 飛び判定用に, 予めすべてのアクションを監視対象にしておく。
        List<CompiledAction<?>> watches = scenario.stream()
                .filter(a -> a.getType() == ScenarioType.ACTION_EXPECT)
                .map(CompiledScenarioAction::getAction)
                .collect(Collectors.toList());
        this.actionManager.getWatcherManager().registerWatchers(
                this.plugin,
                this,
                this.scenario,
                watches,
                WatchType.SCENARIO
        );

        for (int i = 0; i < scenario.size(); i++)
        {
            if (!this.isRunning)
                if (this.isTimedOut())
                    return this.genResult(ScenarioResultCause.RUN_TIMED_OUT);
                else
                    return this.genResult(ScenarioResultCause.CANCELLED);

            CompiledScenarioAction<?> scenarioBean = scenario.get(i);
            CompiledScenarioAction<?> next = i + 1 < scenario.size() ? scenario.get(i + 1): null;

            ScenarioResult result = this.runScenario(scenarioBean, next);
            if (!(result.getScenarioResultCause() == ScenarioResultCause.PASSED || result.getScenarioResultCause() == ScenarioResultCause.SKIPPED))
                return result;
        }

        return null;
    }

    private ScenarioResult runScenario(CompiledScenarioAction<?> scenario, CompiledScenarioAction<?> next)
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

        return this.deliverer.waitResult(scenario.getBean().getTimeout(), this.state);
    }

    private <T extends ActionArgument> void doAction(CompiledScenarioAction<T> scenario, CompiledScenarioAction<?> next)
    {
        this.testReporter.onActionStart(this, scenario);

        // このアクションにより, 次のアクションが起きるかもしれないので、次が EXPECT なら監視対象にする。
        if (next != null && next.getType() == ScenarioType.ACTION_EXPECT)
            this.addWatch(next);

        this.actionManager.queueExecute(scenario.getAction());
    }

    private <T extends ActionArgument> ScenarioResult testCondition(CompiledScenarioAction<T> scenario)
    {
        this.testReporter.onActionStart(this, scenario);

        assert scenario.getAction().getExecutor() instanceof Requireable;
        //noinspection rawtypes
        Requireable requireable = (Requireable) scenario.getAction().getExecutor();

        boolean result;
        try
        {
            //noinspection unchecked
            result = requireable.isConditionFulfilled(scenario.getAction().getArgument(), this);
        }
        catch (Throwable e)
        {
            this.registry.getExceptionHandler().report(e);
            this.testReporter.onConditionCheckFailed(this, scenario);
            return this.genResult(ScenarioResultCause.INTERNAL_ERROR);
        }

        ScenarioResult scenarioResult;
        if (result)
        {
            this.testReporter.onConditionCheckSuccess(this, scenario);
            scenarioResult = this.genResult(ScenarioResultCause.PASSED);
        }
        else
        {
            this.testReporter.onConditionCheckFailed(this, scenario);
            scenarioResult = this.genResult(ScenarioResultCause.ILLEGAL_CONDITION);
        }

        return scenarioResult;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})  // 型は上流で保証されている。
    private void addWatch(CompiledScenarioAction/*<?>*/ scenario)
    {
        if (scenario.getType() != ScenarioType.ACTION_EXPECT)
            throw new IllegalArgumentException("Scenario type must be ACTION_EXPECT.");
        else if (this.watchedActions.contains(scenario))
            return;
        this.watchedActions.add(scenario);

        this.testReporter.onActionStart(this, scenario);
        scenario.getAction().getExecutor().onStartWatching(scenario.getAction().getArgument(), this.plugin, null);
        this.listener.setWaitingFor(scenario);
    }

    @Override
    public void cancel()
    {
        this.deliverer.kill();
        this.cleanUp();  // これの位置を変えると, 排他の問題でバグる
        this.state = ScenarioState.STAND_BY;
    }

    public void setState(ScenarioState state)
    {
        this.state = state;

        // マイルストーンをリセットする。
        this.manager.getMilestoneManager().revokeAllMilestones(this, MilestoneScope.fromState(state));
    }

    @Override
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
            this.cancel();  // RUN_TIMED_OUT の指定は上流の runScenario で行われる。
    }

    private boolean isTimedOut()
    {
        return this.scenario.getTimeout() != -1 && this.elapsedTicks >= this.scenario.getTimeout();
    }

    private void setRunInfo(TriggerBean trigger)
    {
        this.ranBy = trigger;
        this.elapsedTicks = 0;
        this.testID = UUID.randomUUID();
        this.startedAt = System.currentTimeMillis();
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);
        if (!(this.isAutoRun = trigger.getType() != TriggerType.MANUAL_DISPATCH))
            this.logWithPrefix(Level.INFO, LangProvider.get(
                    "scenario.run.manually",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));
        this.deliverer = new ScenarioResultDelivererImpl(this.registry, this.scenario, this.testID, this.startedAt);
        this.watchedActions = new ArrayList<>();

        this.isRunning = true;
    }

    private void logWithPrefix(Level level, String message)
    {
        this.registry.getLogger().log(
                level,
                this.logPrefix + message
        );
    }

    private ScenarioResult genResult(ScenarioResultCause cause)
    {
        return new ScenarioResultImpl(
                this.getScenario(),
                this.testID,
                this.state,
                cause,
                this.startedAt
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ScenarioEngineImpl)) return false;
        ScenarioEngineImpl that = (ScenarioEngineImpl) o;
        return this.plugin.equals(that.plugin) && this.scenario.getName().equals(that.scenario.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.plugin, this.scenario.getName());
    }
}
