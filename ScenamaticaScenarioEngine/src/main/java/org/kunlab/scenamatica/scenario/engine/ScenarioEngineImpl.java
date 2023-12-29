package org.kunlab.scenamatica.scenario.engine;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.enums.MilestoneScope;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ActionResultDeliverer;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenario.ScenarioCompiler;
import org.kunlab.scenamatica.scenario.ScenarioResultImpl;
import org.kunlab.scenamatica.scenario.ScenarioTestReporterBridge;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@Getter
public class ScenarioEngineImpl implements ScenarioEngine
{  // TODO: testID -> runID
    private final ScenamaticaRegistry registry;
    private final boolean verbose;
    private final ScenarioManager manager;
    private final ActionRunManager actionRunManager;
    private final TestReporter testReporter;  // 一意
    private final Plugin plugin;
    private final ScenarioFileStructure scenario;
    private final ScenarioCompiler compiler;
    private final ScenarioActionListener listener;  // エンジンに一意
    private final String logPrefix;
    private final List<? extends CompiledScenarioAction> actions;
    private final List<? extends CompiledTriggerAction> triggerActions;
    private final CompiledScenarioAction runIf;

    private ScenarioExecutor executor;
    private volatile boolean isRunning; // #start(@NotNull TriggerStructure trigger) 内でのみ書き換えられる
    private ScenarioState state;
    private TriggerStructure ranBy;
    private Context context;

    public ScenarioEngineImpl(@NotNull ScenamaticaRegistry registry,
                              @NotNull ScenarioManager manager,
                              @NotNull ActionRunManager actionRunManager,
                              @NotNull TestReporter testReporter,
                              @NotNull Plugin plugin,
                              @NotNull ScenarioFileStructure scenario)
    {
        this.registry = registry;
        this.manager = manager;
        this.actionRunManager = actionRunManager;
        this.testReporter = testReporter;
        this.plugin = plugin;
        this.scenario = scenario;
        this.state = ScenarioState.STAND_BY;
        this.listener = new ScenarioTestReporterBridge(this, this.registry);
        this.verbose = registry.getEnvironment().isVerbose();
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);

        this.executor = null;

        // 以下、 アクションをコンパイルしてキャッシュしておく。
        ScenarioCompiler compiler = this.compiler = new ScenarioCompiler(this, registry.getLogger(), actionRunManager);
        compiler.notifyCompileStart();

        this.actions = compiler.compileMain(scenario.getScenario());
        this.triggerActions = compiler.compileTriggerActions(scenario.getTriggers());
        this.runIf = scenario.getRunIf() == null ? null: compiler.compileRunIf(scenario.getRunIf());
        // トリガの準備
        registry.getTriggerManager().bakeTriggers(this);
    }

    /**
     * コンテキストを取得します。
     *
     * @return ステージ
     */
    @Override
    @NotNull
    public Context getContext()
    {
        return Objects.requireNonNull(this.context, "context is null");
    }

    @Override
    @NotNull  // TODO: TriggerStructure to TriggerType
    public ScenarioResult start(@NotNull TriggerStructure trigger, @NotNull SessionStorage variable, int attemptedCount) throws TriggerNotFoundException
    {
        this.ranBy = trigger;
        if (!this.isAutoRun())
            this.logWithPrefix(Level.INFO, LangProvider.get(
                            "scenario.run.manually",
                            MsgArgs.of("scenarioName", this.scenario.getName())
                    ),
                    true
            );

        this.executor = new ScenarioExecutor(
                this,
                this.actionRunManager,
                this.listener,
                this.actions,
                this.triggerActions,
                this.runIf,
                variable,
                attemptedCount
        );

        this.isRunning = true;
        ScenarioResult result = this.start$1(trigger, attemptedCount);
        this.isRunning = false;  // これの位置を変えると, 排他の問題でバグる
        ThreadingUtil.waitFor(this.registry, this::cleanUp);

        return result;
    }

    private ScenarioResult start$1(@NotNull TriggerStructure trigger, int attemptedCount) throws TriggerNotFoundException
    {
        this.testReporter.onTestStart(this, trigger);

        // あとかたづけ は、できるだけ明瞭にしたいのでこのメソッド内で完結する。

        this.context = this.prepareContext();  // State 変更: CONTEXT_PREPARING
        if (this.context == null)
            return new ScenarioResultImpl(
                    this.scenario,
                    this.executor.getTestID(),
                    this.state,
                    ScenarioResultCause.CONTEXT_PREPARATION_FAILED,
                    Collections.emptyList(),
                    this.executor.getStartedAt(),
                    attemptedCount
            );

        this.logWithPrefix(Level.INFO, LangProvider.get(
                        "scenario.run.engine.starting",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                ),
                true
        );

        this.state = ScenarioState.STARTING;

        return this.executor.start(trigger);
    }

    @Nullable
    private Context prepareContext()
    {
        this.state = ScenarioState.CONTEXT_PREPARING;

        try
        {
            return this.registry.getContextManager().prepareContext(this.scenario, this.executor.getTestID());
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            this.logWithPrefix(Level.WARNING, LangProvider.get(
                            "scenario.run.prepare.fail",
                            MsgArgs.of("scenarioName", this.scenario.getName())
                    ),
                    false
            );

            return null;
        }
    }

    private void cleanUp()
    {
        this.state = ScenarioState.CLEANING_UP;
        this.logWithPrefix(Level.INFO, LangProvider.get(
                        "scenario.run.prepare.destroy",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                ),
                true
        );
        this.context.destroy();

        this.executor = null;
        this.ranBy = null;
        this.context = null;
        this.state = ScenarioState.STAND_BY;
    }

    @Override
    public void cancel()
    {
        this.executor.cancel();
        this.cleanUp();  // これの位置を変えると, 排他の問題でバグる
        this.isRunning = false;
        this.state = ScenarioState.STAND_BY;
    }

    @Override
    public void onTick()
    {
        this.executor.onTick();
    }

    /**
     * 与えられた一意のテスト ID を取得します。
     *
     * @return テスト ID
     */
    @Override
    public UUID getTestID()
    {
        if (this.executor == null)
            return null;
        else
            return this.executor.getTestID();
    }

    /**
     * シナリオが開始された時間を取得します。
     *
     * @return 開始時間
     */
    @Override
    public long getStartedAt()
    {
        if (this.executor == null)
            return -1;
        else
            return this.executor.getStartedAt();
    }

    /**
     * シナリオが自動実行されたかどうかを取得します。
     *
     * @return 自動実行されたかどうか
     */
    @Override
    public boolean isAutoRun()
    {
        if (this.ranBy == null)
            return true;
        else
            return this.ranBy.getType() != TriggerType.MANUAL_DISPATCH;
    }

    public ScenarioState getState()
    {
        if (this.executor == null)
            return this.state;

        // STARTING 以降は executor のみが操作するので、 そっちを返す。
        return this.state == ScenarioState.STARTING ? this.executor.getState(): this.state;
    }

    public void setState(ScenarioState state)
    {
        this.state = state;

        // マイルストーンをリセットする。
        this.manager.getMilestoneManager().revokeAllMilestones(this, MilestoneScope.fromState(state));
    }

    /**
     * コンパイルされたシナリオを取得します。
     *
     * @return コンパイルされたシナリオ
     */
    @Override
    public CompiledScenarioAction getCurrentScenario()
    {
        return this.executor.getCurrentScenario();
    }

    /**
     * シナリオの結果を受け取るオブジェクトを取得します。
     *
     * @return シナリオの結果を受け取るオブジェクト
     */
    @Override
    public ActionResultDeliverer getDeliverer()
    {
        return this.executor.getDeliverer();
    }

    private void logWithPrefix(Level level, String message, boolean onlyVerbose)
    {
        if (onlyVerbose && !this.verbose)
            return;
        this.registry.getLogger().log(level, this.logPrefix + message);
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
