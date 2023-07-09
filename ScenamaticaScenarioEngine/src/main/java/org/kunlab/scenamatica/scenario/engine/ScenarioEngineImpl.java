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
import org.kunlab.scenamatica.interfaces.action.ActionManager;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResultDeliverer;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.kunlab.scenamatica.scenario.ScenarioActionListenerImpl;
import org.kunlab.scenamatica.scenario.ScenarioCompiler;
import org.kunlab.scenamatica.scenario.ScenarioResultImpl;

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
    private final ActionManager actionManager;
    private final TestReporter testReporter;  // 一意
    private final Plugin plugin;
    private final ScenarioFileBean scenario;
    private final ScenarioCompiler compiler;
    private final ScenarioActionListener listener;  // エンジンに一意
    private final String logPrefix;
    private final List<? extends CompiledScenarioAction<?>> actions;
    private final List<? extends CompiledTriggerAction> triggerActions;
    private final CompiledScenarioAction<?> runIf;

    private ScenarioExecutor executor;
    private volatile boolean isRunning; // #start(@NotNull TriggerBean trigger) 内でのみ書き換えられる
    private ScenarioState state;
    private TriggerBean ranBy;
    private Context context;

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
        this.state = ScenarioState.STAND_BY;
        this.listener = new ScenarioActionListenerImpl(this, this.registry);
        this.verbose = registry.getEnvironment().isVerbose();
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);

        this.executor = null;

        // 以下、 アクションをコンパイルしてキャッシュしておく。
        ScenarioCompiler compiler = this.compiler = new ScenarioCompiler(this, registry, actionManager);
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
    public @NotNull Context getContext()
    {
        return Objects.requireNonNull(this.context, "context is null");
    }

    @Override
    @NotNull  // TODO: TriggerBean to TriggerType
    public ScenarioResult start(@NotNull TriggerBean trigger) throws TriggerNotFoundException
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
                this.actionManager,
                this.listener,
                this.actions,
                this.triggerActions,
                this.runIf
        );

        this.isRunning = true;
        ScenarioResult result = this.start$1(trigger);
        this.isRunning = false;  // これの位置を変えると, 排他の問題でバグる
        this.cleanUp();

        return result;
    }

    private ScenarioResult start$1(@NotNull TriggerBean trigger) throws TriggerNotFoundException
    {
        this.testReporter.onTestStart(this, trigger);

        // あとかたづけ は、できるだけ明瞭にしたいのでこのメソッド内で完結する。

        this.context = this.prepareContext();  // State 変更: CONTEXT_PREPARING
        if (this.context == null)
        {
            // あとかたづけ
            ThreadingUtil.waitFor(this.registry, this::cleanUp);
            return new ScenarioResultImpl(
                    this.scenario,
                    this.executor.getTestID(),
                    this.state,
                    ScenarioResultCause.CONTEXT_PREPARATION_FAILED,
                    this.executor.getStartedAt()
            );
        }

        this.logWithPrefix(Level.INFO, LangProvider.get(
                        "scenario.run.engine.starting",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                ),
                true
        );

        this.state = ScenarioState.STARTING;

        ScenarioResult result = this.executor.start(trigger);
        ThreadingUtil.waitFor(this.registry, this::cleanUp);
        return result;
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
        this.registry.getContextManager().destroyContext();

        this.executor = null;
        this.ranBy = null;
        this.state = ScenarioState.STAND_BY;
    }

    @Override
    public void cancel()
    {
        this.executor.cancel();
        this.cleanUp();  // これの位置を変えると, 排他の問題でバグる
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
    public CompiledScenarioAction<?> getCurrentScenario()
    {
        return this.executor.getCurrentScenario();
    }

    /**
     * シナリオの結果を受け取るオブジェクトを取得します。
     *
     * @return シナリオの結果を受け取るオブジェクト
     */
    @Override
    public ScenarioResultDeliverer getDeliverer()
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
