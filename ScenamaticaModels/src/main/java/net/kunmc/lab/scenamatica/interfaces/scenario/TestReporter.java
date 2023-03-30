package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
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
     * @param scenario シナリオ
     * @param trigger  シナリオのトリガ
     */
    void onTestStart(@NotNull ScenarioFileBean scenario, @NotNull TriggerBean trigger);

    /**
     * シナリオのアクションの開始をレポートします。
     *
     * @param scenario シナリオ
     * @param action   アクション
     */
    void onActionStart(@NotNull ScenarioFileBean scenario, @NotNull CompiledAction<?> action);

    /**
     * シナリオのアクションの監視の開始をレポートします。
     *
     * @param scenario シナリオ
     * @param action   アクション
     */
    void onActionWatchStart(@NotNull ScenarioFileBean scenario, @NotNull CompiledAction<?> action);

    /**
     * シナリオのアクションが正常に実行されたことをレポートします。
     *
     * @param scenario シナリオ
     * @param action   アクション
     */
    void onActionSuccess(@NotNull ScenarioFileBean scenario, @NotNull CompiledAction<?> action);

    /**
     * 監視していたアクションが正常に実行されたことをレポートします。
     *
     * @param scenario シナリオ
     * @param action   アクション
     */
    void onWatchingActionExecuted(@NotNull ScenarioFileBean scenario, @NotNull Action<?> action);

    /**
     * 監視していたアクションがジャンプして実行されたことをレポートします。
     *
     * @param scenario シナリオ
     * @param action   アクション
     * @param expected 期待されるアクション
     */
    void onActionJumped(@NotNull ScenarioFileBean scenario, @NotNull Action<?> action, @NotNull CompiledScenarioAction<?> expected);

    /**
     * アクションの実行に失敗したことをレポートします。
     *
     * @param scenario シナリオ
     * @param action   アクション
     */
    void onActionExecuteFailed(@NotNull ScenarioFileBean scenario, @NotNull CompiledAction<?> action, @NotNull Throwable error);

    /**
     * テストが終了したことをレポートします。
     *
     * @param scenario シナリオ
     * @param result   テスト結果
     */
    void onTestEnd(@NotNull ScenarioFileBean scenario, @NotNull TestResult result);

}
