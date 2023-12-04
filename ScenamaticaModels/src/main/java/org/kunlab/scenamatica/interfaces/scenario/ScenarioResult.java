package org.kunlab.scenamatica.interfaces.scenario;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.UUID;

/**
 * テスト結果を表すインターフェースです。
 */
public interface ScenarioResult
{
    /**
     * テスト ID を取得します。
     *
     * @return テスト ID
     */
    @NotNull
    UUID getTestID();

    /**
     * テストのシナリオを取得します。
     *
     * @return テストのシナリオ
     */
    @NotNull
    ScenarioFileStructure getScenario();

    /**
     * テストの状態を取得します。
     *
     * @return テストの状態
     */
    @NotNull
    ScenarioState getState();

    /**
     * テストの結果を取得します。
     *
     * @return テストの結果
     */
    @NotNull
    ScenarioResultCause getScenarioResultCause();

    /**
     * テストが開始された時間を取得します。
     *
     * @return テストが開始された時間
     */
    long getStartedAt();

    /**
     * テストが終了した時間を取得します。
     *
     * @return テストが終了した時間
     */
    long getFinishedAt();

    /**
     * このテストのセッション内での試行回数を取得します。
     *
     * @return このテストのセッション内での試行回数
     */
    int getAttemptOf();

    /**
     * テストに失敗した動作を取得します。
     *
     * @return テストに失敗した動作
     */
    @Nullable
    Action<?> getFailedAction();
}
