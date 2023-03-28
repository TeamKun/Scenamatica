package net.kunmc.lab.scenamatica.scenario;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.scenario.runtime.CompiledTriggerActionImpl;
import net.kunmc.lab.scenamatica.scenario.runtime.Compilers;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@Getter
public class ScenarioEngineImpl implements ScenarioEngine
{
    private final ScenamaticaRegistry registry;
    private final ActionManager actionManager;
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

    public ScenarioEngineImpl(@NotNull ScenamaticaRegistry registry,
                              @NotNull ActionManager actionManager,
                              @NotNull Plugin plugin,
                              @NotNull ScenarioFileBean scenario)
    {
        this.registry = registry;
        this.actionManager = actionManager;
        this.plugin = plugin;
        this.scenario = scenario;
        this.state = TestState.STAND_BY;
        this.listener = new ScenarioActionListenerImpl(this);

        // アクションをコンパイルしてキャッシュしておく。

        this.log(Level.INFO, "Starting scenario compilation for scenario \"{}\" ...", this.scenario.getName());

        int compileNeeded = getCompileNeeded(this.scenario.getTriggers());
        int compiled = 0;

        // 本シナリオのコンパイル
        this.log(Level.INFO, "[{}/{}] Compiling scenario MAIN of scenario \"{}\" ...",
                ++compiled, compileNeeded, this.scenario.getName()
        );
        this.actions = this.runCompiler(this.scenario.getScenario());

        // トリガの before/after のコンパイル
        this.triggerActions = this.compileTriggerActions(scenario.getTriggers(), compileNeeded);
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

            TriggerType type = trigger.getType();
            if (!trigger.getBeforeThat().isEmpty())
            {
                this.log(Level.INFO, "[{}/{}] Compiling scenario {}:BEFORE of scenario \"{}\" ..."
                        , ++compiled, compileNeeded, type, this.scenario.getName()
                );
                beforeActions = this.runCompiler(trigger.getBeforeThat());
            }
            else
                beforeActions = new ArrayList<>();

            if (!trigger.getAfterThat().isEmpty())
            {
                this.log(Level.INFO, "[{}/{}] Compiling scenario {}:AFTER of scenario \"{}\" ...",
                        ++compiled, compileNeeded, type, this.scenario.getName()
                );
                afterActions = this.runCompiler(trigger.getAfterThat());
            }
            else
                afterActions = new ArrayList<>();

            triggerActions.add(new CompiledTriggerActionImpl(trigger, beforeActions, afterActions));
        }

        return triggerActions;
    }

    @Override
    public TestResult start(TriggerBean trigger)
    {
        this.setRunInfo(trigger);
        CompiledTriggerAction compiledTrigger = this.triggerActions.parallelStream()
                .filter(t -> t.getTrigger().getType() == trigger.getType())
                .filter(t -> Objects.equals(t.getTrigger().getArgument(), trigger.getArgument()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Invalid trigger: " + trigger));

        this.state = TestState.CONTEXT_PREPARING;
        Context context = this.registry.getContextManager().prepareContext(this.scenario, this.testID);
        if (context == null)
        {
            this.log(Level.WARNING, "Failed to prepare context.");
            this.isRunning = false;
            return new TestResultImpl(
                    this.testID,
                    this.state,
                    TestResultCause.CONTEXT_PREPARATION_FAILED,
                    "Failed to prepare context.",
                    this.startedAt
            );
        }

        this.log(Level.INFO, "Starting scenario engine for scenario \"{}\" ...", this.scenario.getName());
        this.state = TestState.STARTING;

        TestResult mayResult = this.runBeforeIfPresent(compiledTrigger);
        if (mayResult != null)
        {
            this.isRunning = false;
            return mayResult;
        }

        this.state = TestState.RUNNING_MAIN;
        this.log(Level.INFO, "Running scenario \"{}\" ...", this.scenario.getName());
        mayResult = this.runScenario(this.actions);
        if (mayResult != null)
        {
            this.isRunning = false;
            return mayResult;
        }

        mayResult = this.runAfterIfPresent(compiledTrigger);
        if (mayResult != null)
        {
            this.isRunning = false;
            return mayResult;
        }

        this.isRunning = false;
        return new TestResultImpl(
                this.testID,
                this.state = TestState.FINISHED,
                TestResultCause.PASSED,
                "Scenario finished successfully.",
                this.startedAt
        );
    }

    private TestResult runBeforeIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getBeforeThat().isEmpty())
        {
            this.state = TestState.RUNNING_BEFORE;
            this.log(Level.INFO, "Running before scenarios ...", this.scenario.getName());
            return this.runScenario(trigger.getBeforeActions());
        }

        return null;
    }

    private TestResult runAfterIfPresent(CompiledTriggerAction trigger)
    {
        if (!trigger.getTrigger().getAfterThat().isEmpty())
        {
            this.state = TestState.RUNNING_AFTER;
            this.log(Level.INFO, "Running after scenarios ...", this.scenario.getName());
            return this.runScenario(trigger.getAfterActions());
        }

        return null;
    }

    @Nullable
    private TestResult runScenario(List<? extends CompiledScenarioAction<?>> scenario)
    {
        // 飛び判定用に, 予めすべてのアクションを監視対象にしておく。
        this.actionManager.getWatcherManager().registerWatchers(
                this.plugin,
                this,
                this.scenario,
                toActions(scenario),
                WatchType.SCENARIO
        );

        for (CompiledScenarioAction<?> scenarioBean : scenario)
        {
            if (!this.isRunning)
                return new TestResultImpl(
                        this.testID,
                        this.state,
                        TestResultCause.CANCELLED,
                        "Scenario cancelled.",
                        this.startedAt
                );

            TestResult result = this.runScenario(scenarioBean);
            if (result.getTestResultCause() != TestResultCause.PASSED)
                return result;
        }

        return null;
    }

    private TestResult runScenario(CompiledScenarioAction<?> scenario)
    {
        this.currentScenario = scenario;
        ScenarioType type = scenario.getType();
        if (Objects.requireNonNull(type) == ScenarioType.ACTION_EXECUTE)
        {
            scenario.execute(this.actionManager, this.listener);
        }

        return this.deliverer.waitResult(this.state);
    }

    @Override
    public void cancel()
    {
        this.isRunning = false;
        this.deliverer.kill();
    }

    private void setRunInfo(TriggerBean trigger)
    {
        this.isRunning = true;
        this.ranBy = trigger;
        this.testID = UUID.randomUUID();
        this.startedAt = System.currentTimeMillis();
        this.logPrefix = "TEST-" + StringUtils.substring(this.scenario.getName(), 0, 8) +
                "/" + this.testID.toString().substring(0, 8);
        if (!(this.isAutoRun = trigger.getType() != TriggerType.MANUAL_DISPATCH))
            this.log(Level.INFO, "The scenario \"{}\" dispatched manually.", this.scenario.getName());
        this.deliverer = new ScenarioResultDeliverer(this.registry, this.testID, this.startedAt);
    }

    void log(Level level, String message, Object... args)
    {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = this.logPrefix;
        System.arraycopy(args, 0, newArgs, 1, args.length);

        this.registry.getLogger().log(level, "[{}] " + message, newArgs);
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
