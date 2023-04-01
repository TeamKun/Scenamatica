package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;

/**
 * シナリオのテストの実行と結果を報告するためのインターフェースです。
 */
public interface TestReporter
{
    /**
     * シナリオのテストの開始をレポートします。
     *
     * @param engine  エンジン
     * @param trigger シナリオのトリガ
     */
    void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerBean trigger);

    /**
     * シナリオのアクションの開始をレポートします。
     *
     * @param engine  エンジン
     * @param action   アクション
     */
    void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action);

    /**
     * シナリオのアクションの監視の開始をレポートします。
     *
     * @param engine  エンジン
     * @param action   アクション
     */
    void onActionWatchStart(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action);

    /**
     * シナリオのアクションが正常に実行されたことをレポートします。
     *
     * @param engine  エンジン
     * @param action   アクション
     */
    void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action);

    /**
     * 監視していたアクションが正常に実行されたことをレポートします。
     *
     * @param engine  エンジン
     * @param action   アクション
     */
    void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull Action<?> action);

    /**
     * 監視していたアクションがジャンプして実行されたことをレポートします。
     *
     * @param engine  エンジン
     * @param action   アクション
     * @param expected 期待されるアクション
     */
    void onActionJumped(@NotNull ScenarioEngine engine, @NotNull Action<?> action, @NotNull CompiledScenarioAction<?> expected);

    /**
     * アクションの実行に失敗したことをレポートします。
     *
     * @param engine  エンジン
     * @param action   アクション
     */
    void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull Throwable error);

    /**
     * テストが終了したことをレポートします。
     *
     * @param engine  エンジン
     * @param result   テスト結果
     */
    void onTestEnd(@NotNull ScenarioEngine engine, @NotNull TestResult result);

}
