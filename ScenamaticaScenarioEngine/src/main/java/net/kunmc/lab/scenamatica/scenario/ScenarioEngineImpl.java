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
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
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
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.scenario.runtime.CompiledTriggerActionImpl;
import net.kunmc.lab.scenamatica.scenario.runtime.Compilers;
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
    private final ScenarioActionListener listener;
    private final List<CompiledScenarioAction<?>> actions;
    private final List<CompiledTriggerAction> triggerActions;

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
        this.state = TestState.STAND_BY;
        this.listener = new ScenarioActionListenerImpl(this, registry);

        String scenarioName = this.scenario.getName();
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);

        // アクションをコンパイルしてキャッシュしておく。

        this.logWithPrefix(
                Level.INFO,
                LangProvider.get("scenario.compile.start", MsgArgs.of("scenarioName", scenarioName))
        );

        int compileNeeded = getCompileNeeded(this.scenario.getTriggers());
        int compiled = 0;

        // 本シナリオのコンパイル
        this.logCompilingMain(++compiled, compileNeeded);

        this.actions = this.runCompiler(this.scenario.getScenario());

        // トリガの before/after のコンパイル
        this.triggerActions = this.compileTriggerActions(scenario.getTriggers(), compileNeeded);

        this.registry.getTriggerManager().bakeTriggers(this);
    }

    private static List<Pair<Action<?>, ActionArgument>> toActions(List<? extends CompiledScenarioAction<?>> actions)
    {
        ArrayList<Pair<Action<?>, ActionArgument>> list = new ArrayList<>(actions.size());
        for (CompiledScenarioAction<?> action : actions)
            list.add(new Pair<>(action.getAction(), action.getArgument()));

        return list;
    }

    private List<CompiledScenarioAction<?>> runCompiler(List<? extends ScenarioBean> scenarios)
    {
        return Compilers.compileActions(
                this.registry,
                this,
                this.actionManager.getCompiler(),
                this.listener,
                scenarios
        );
    }

    private int getCompileNeeded(List<? extends TriggerBean> triggers)
    {
        int compileNeeded = 1; // 本シナリオの分。

        for (TriggerBean trigger : triggers)
        {
            if (!trigger.getBeforeThat().isEmpty())
                compileNeeded++;
            if (!trigger.getAfterThat().isEmpty())
                compileNeeded++;
        }

        return compileNeeded;
    }

    private List<CompiledTriggerAction> compileTriggerActions(List<? extends TriggerBean> triggers, int compileNeeded)
    {
        int compiled = 1; // 本シナリオの分。

        List<CompiledTriggerAction> triggerActions = new ArrayList<>();
        for (TriggerBean trigger : triggers)
        {
            List<CompiledScenarioAction<?>> beforeActions;
            List<CompiledScenarioAction<?>> afterActions;

            TriggerType triggerType = trigger.getType();
            if (!trigger.getBeforeThat().isEmpty())
            {
                this.logCompiling(++compiled, compileNeeded, "BEFORE", triggerType);
                beforeActions = this.runCompiler(trigger.getBeforeThat());
            }
            else
                beforeActions = new ArrayList<>();

            if (!trigger.getAfterThat().isEmpty())
            {
                this.logCompiling(++compiled, compileNeeded, "AFTER", triggerType);
                afterActions = this.runCompiler(trigger.getAfterThat());
            }
            else
                afterActions = new ArrayList<>();

            triggerActions.add(new CompiledTriggerActionImpl(trigger, beforeActions, afterActions));
        }

        return triggerActions;
    }

    @Override
    @NotNull
    @SneakyThrows(InterruptedException.class)
    public TestResult start(@NotNull TriggerBean trigger) throws TriggerNotFoundException
    {
        this.setRunInfo(trigger);
        CompiledTriggerAction compiledTrigger = this.triggerActions.parallelStream()
                .filter(t -> t.getTrigger().getType() == trigger.getType())
                .filter(t -> Objects.equals(t.getTrigger().getArgument(), trigger.getArgument()))
                .findFirst().orElseThrow(() -> new TriggerNotFoundException(trigger.getType()));

        this.state = TestState.CONTEXT_PREPARING;
        Context context;
        try
        {
            context = this.registry.getContextManager().prepareContext(this.scenario, this.testID);
            if (context == null)
                throw new ContextPreparationException(""); // だみー
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            this.logWithPrefix(Level.WARNING, LangProvider.get(
                    "scenario.run.prepare.fail",
                    MsgArgs.of("scenarioName", this.scenario.getName())
            ));

            // あとかたづけ
            ThreadingUtil.waitFor(this.registry.getPlugin(), this::cleanUp);

            return new TestResultImpl(
                    this.testID,
                    this.state,
                    TestResultCause.CONTEXT_PREPARATION_FAILED,
                    this.startedAt
            );
        }


        this.logWithPrefix(Level.INFO, LangProvider.get(
                "scenario.run.engine.starting",
                MsgArgs.of("scenarioName", this.scenario.getName())
        ));
        this.state = TestState.STARTING;
        Thread.sleep(200); // アクションとの排他制御のためにちょっと待つ。ロードしてる風でごめんね ><

        TestResult mayResult = this.runBeforeIfPresent(compiledTrigger);
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

        return new TestResultImpl(
                this.testID,
                this.state = TestState.FINISHED,
                TestResultCause.PASSED,
                this.startedAt
        );
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
                return new TestResultImpl(
                        this.testID,
                        this.state,
                        TestResultCause.CANCELLED,
                        this.startedAt
                );

            CompiledScenarioAction<?> scenarioBean = scenario.get(i);
            CompiledScenarioAction<?> next = i + 1 < scenario.size() ? scenario.get(i + 1): null;

            TestResult result = this.runScenario(scenarioBean, next);
            if (result.getTestResultCause() != TestResultCause.PASSED)
                return result;
        }

        return null;
    }

    private TestResult runScenario(CompiledScenarioAction<?> scenario, CompiledScenarioAction<?> next)
    {
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
            return new TestResultImpl(
                    this.testID,
                    this.state,
                    TestResultCause.INTERNAL_ERROR,
                    this.startedAt
            );
        }

        TestResult testResult;
        if (result)
        {
            this.testReporter.onConditionCheckSuccess(this, scenario);
            testResult = new TestResultImpl(
                    this.testID,
                    this.state,
                    TestResultCause.PASSED,
                    this.startedAt
            );
        }
        else
        {
            this.testReporter.onConditionCheckFailed(this, scenario);
            testResult = new TestResultImpl(
                    this.testID,
                    this.state,
                    TestResultCause.ILLEGAL_CONDITION,
                    this.startedAt
            );
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

    private void logCompiling(int compiled, int total, String type, TriggerType triggerType)
    {
        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", type)
                .add("triggerType", triggerType.name())
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.child", newArgs));
    }

    private void logCompilingMain(int compiled, int total)
    {
        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", "main")
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.main", newArgs));
    }

    private void logWithPrefix(Level level, String message)
    {
        this.registry.getLogger().log(
                level,
                this.logPrefix + message
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
