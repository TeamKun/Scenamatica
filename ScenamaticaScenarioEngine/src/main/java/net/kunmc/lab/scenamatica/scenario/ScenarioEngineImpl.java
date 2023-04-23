package net.kunmc.lab.scenamatica.scenario;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.scenamatica.commons.utils.LogUtils;
import net.kunmc.lab.scenamatica.commons.utils.ThreadingUtil;
import net.kunmc.lab.scenamatica.enums.MilestoneScope;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResultDeliverer;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
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
    private TriggerBean ranBy;
    private UUID testID;
    private long startedAt;
    private String logPrefix;
    private boolean isAutoRun;
    private TestState state;
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
        this.state = TestState.STAND_BY;

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
    @SneakyThrows(InterruptedException.class)
    public TestResult start(@NotNull TriggerBean trigger) throws TriggerNotFoundException
    {
        this.setRunInfo(trigger);
        CompiledTriggerAction compiledTrigger = this.findTriggerOrThrow(trigger);

        // あとかたづけ は、できるだけ明瞭にしたいのでこのメソッド内で完結する。

        Context context = this.prepareContext();  // State 変更: CONTEXT_PREPARING
        if (context == null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry.getPlugin(), this::cleanUp);
            this.genResult(TestResultCause.CONTEXT_PREPARATION_FAILED);
        }

        this.logWithPrefix(Level.INFO, LangProvider.get(
                "scenario.run.engine.starting",
                MsgArgs.of("scenarioName", this.scenario.getName())
        ));
        this.state = TestState.STARTING;
        Thread.sleep(200); // アクションとの排他制御のためにちょっと待つ。ロードしてる風でごめんね ><

        TestResult result = this.startScenarioRun(compiledTrigger);
        this.state = TestState.FINISHED;

        return result;
    }

    private TestResult startScenarioRun(@NotNull CompiledTriggerAction compiledTrigger)
    {
        TestResult mayResult = this.testRunConditionIfExists(this.runIf);
        if (mayResult != null)
            return mayResult;  // あとかたづけは上に包含されている。

        mayResult = this.testRunConditionIfExists(compiledTrigger.getRunIf());
        if (mayResult != null)
            return mayResult;  // あとかたづけは上に包含されている。

        mayResult = this.runBeforeIfPresent(compiledTrigger);
        if (mayResult != null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry.getPlugin(), this::cleanUp);
            return mayResult;
        }

        this.state = TestState.RUNNING_MAIN;
        this.logWithPrefix(Level.INFO, LangProvider.get(

                "scenario.run.starting.main",
                MsgArgs.of("scenarioName", this.scenario.getName())
        ));
        mayResult = this.runScenario(this.actions);
        if (mayResult != null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry.getPlugin(), this::cleanUp);
            return mayResult;
        }

        mayResult = this.runAfterIfPresent(compiledTrigger);
        if (mayResult != null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry.getPlugin(), this::cleanUp);
            return mayResult;
        }

        // あとかたづけ
        ThreadingUtil.waitFor(this.registry.getPlugin(), this::cleanUp);

        return this.genResult(TestResultCause.PASSED);
    }

    private TestResult testRunConditionIfExists(@Nullable CompiledScenarioAction<?> runIf)
    {
        if (runIf != null)
        {
            TestResult conditionResult = this.testCondition(runIf);
            // コンディションチェックに失敗した(満たしていない)場合は(エラーにはせずに)スキップする。
            if (conditionResult.getTestResultCause() != TestResultCause.PASSED)
            {
                this.testReporter.onTestSkipped(this, runIf);

                // あとかたづけ
                ThreadingUtil.waitFor(this.registry.getPlugin(), this::cleanUp);
                return this.genResult(TestResultCause.SKIPPED); // 実質的にはエラーではないので、スキップする。
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
        this.state = TestState.CONTEXT_PREPARING;

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
        this.state = TestState.CLEANING_UP;

        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);
        // シナリオのアクションの監視を全て解除しておく。
        this.actionManager.getWatcherManager().unregisterWatchers(this.plugin, WatchType.SCENARIO);
        this.logWithPrefix(Level.INFO, LangProvider.get(
                "scenario.run.prepare.destroy",
                MsgArgs.of("scenarioName", this.scenario.getName())
        ));
        this.registry.getContextManager().destroyContext();

        this.isRunning = false;  // これの位置を変えると, 排他の問題でバグる
    }

    private TestResult runBeforeIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getBeforeThat().isEmpty())
        {
            this.state = TestState.RUNNING_BEFORE;
            this.logWithPrefix(Level.INFO, LangProvider.get(
                    "scenario.run.starting.before",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));
            return this.runScenario(trigger.getBeforeActions());
        }

        return null;
    }

    private TestResult runAfterIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getAfterThat().isEmpty())
        {
            this.state = TestState.RUNNING_AFTER;
            this.logWithPrefix(Level.INFO, LangProvider.get(
                    "scenario.run.starting.after",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));
            return this.runScenario(trigger.getAfterActions());
        }

        return null;
    }

    @Nullable
    private TestResult runScenario(List<? extends CompiledScenarioAction<?>> scenario)
    {
        // 飛び判定用に, 予めすべてのアクションを監視対象にしておく。
        List<CompiledScenarioAction<?>> watches = scenario.stream()
                .filter(a -> a.getType() == ScenarioType.ACTION_EXPECT)
                .collect(Collectors.toList());
        this.actionManager.getWatcherManager().registerWatchers(
                this.plugin,
                this,
                this.scenario,
                toActions(watches),  // 同じ Stream にすると, 型の関係で可読性が低下する。パフォーマンス影響は軽微なので無視。
                WatchType.SCENARIO
        );

        for (int i = 0; i < scenario.size(); i++)
        {
            if (!this.isRunning)
                return this.genResult(TestResultCause.CANCELLED);

            CompiledScenarioAction<?> scenarioBean = scenario.get(i);
            CompiledScenarioAction<?> next = i + 1 < scenario.size() ? scenario.get(i + 1): null;

            TestResult result = this.runScenario(scenarioBean, next);
            if (!(result.getTestResultCause() == TestResultCause.PASSED || result.getTestResultCause() == TestResultCause.SKIPPED))
                return result;
        }

        return null;
    }

    private static List<Pair<Action<?>, ActionArgument>> toActions(List<? extends CompiledScenarioAction<?>> actions)
    {
        ArrayList<Pair<Action<?>, ActionArgument>> list = new ArrayList<>(actions.size());
        for (CompiledScenarioAction<?> action : actions)
            list.add(new Pair<>(action.getAction(), action.getArgument()));

        return list;
    }

    private TestResult runScenario(CompiledScenarioAction<?> scenario, CompiledScenarioAction<?> next)
    {
        if (scenario.getRunIf() != null)
        {
            TestResult result = this.testCondition(scenario.getRunIf());
            if (result.getTestResultCause() != TestResultCause.PASSED)
                return this.genResult(TestResultCause.SKIPPED);
        }

        this.currentScenario = scenario;
        ScenarioType type = scenario.getType();

        switch (type)
        {
            case ACTION_EXECUTE:
                this.testReporter.onActionStart(this, scenario);

                // このアクションにより, 次のアクションが起きるかもしれないので、次が EXPECT なら監視対象にする。
                if (next != null && next.getType() == ScenarioType.ACTION_EXPECT)
                    this.addWatch(next);

                scenario.execute(this, this.actionManager, this.listener);
                break;
            case ACTION_EXPECT:
                this.addWatch(scenario);
                break;
            case CONDITION_REQUIRE:
                return this.testCondition(scenario);
        }

        return this.deliverer.waitResult(scenario.getBean().getTimeout(), this.state);
    }

    private <T extends ActionArgument> TestResult testCondition(CompiledScenarioAction<T> scenario)
    {
        this.testReporter.onConditionCheckStart(this, scenario);

        assert scenario.getAction() instanceof Requireable;
        //noinspection rawtypes
        Requireable requireable = (Requireable) scenario.getAction();

        boolean result;
        try
        {
            //noinspection unchecked
            result = requireable.isConditionFulfilled(scenario.getArgument(), this);
        }
        catch (Throwable e)
        {
            this.registry.getExceptionHandler().report(e);
            this.testReporter.onConditionCheckFailed(this, scenario);
            return this.genResult(TestResultCause.INTERNAL_ERROR);
        }

        TestResult testResult;
        if (result)
        {
            this.testReporter.onConditionCheckSuccess(this, scenario);
            testResult = this.genResult(TestResultCause.PASSED);
        }
        else
        {
            this.testReporter.onConditionCheckFailed(this, scenario);
            testResult = this.genResult(TestResultCause.ILLEGAL_CONDITION);
        }

        return testResult;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})  // 型は上流で保証されている。
    private void addWatch(CompiledScenarioAction/*<?>*/ scenario)
    {
        if (scenario.getType() != ScenarioType.ACTION_EXPECT)
            throw new IllegalArgumentException("Scenario type must be ACTION_EXPECT.");
        else if (this.watchedActions.contains(scenario))
            return;
        this.watchedActions.add(scenario);

        this.testReporter.onActionWatchStart(this, scenario);
        scenario.getAction().onStartWatching(scenario.getArgument(), this.plugin, null);
        this.listener.setWaitingFor(scenario);
    }

    @Override
    public void cancel()
    {
        this.deliverer.kill();
        this.cleanUp();  // これの位置を変えると, 排他の問題でバグる
        this.state = TestState.STAND_BY;
    }

    public void setState(TestState state)
    {
        this.state = state;

        // マイルストーンをリセットする。
        this.manager.getMilestoneManager().revokeAllMilestones(this, MilestoneScope.fromState(state));
    }

    private void setRunInfo(TriggerBean trigger)
    {
        this.ranBy = trigger;
        this.testID = UUID.randomUUID();
        this.startedAt = System.currentTimeMillis();
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);
        if (!(this.isAutoRun = trigger.getType() != TriggerType.MANUAL_DISPATCH))
            this.logWithPrefix(Level.INFO, LangProvider.get(
                    "scenario.run.manually",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));
        this.deliverer = new ScenarioResultDelivererImpl(this.registry, this.testID, this.startedAt);
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

    private TestResult genResult(TestResultCause cause)
    {
        return new TestResultImpl(
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
