package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * テスト結果を表すインターフェースです。
 */
public interface TestResult
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
    ScenarioFileBean getScenario();

    /**
     * テストの状態を取得します。
     *
     * @return テストの状態
     */
    @NotNull
    TestState getState();

    /**
     * テストの結果を取得します。
     *
     * @return テストの結果
     */
    @NotNull
    TestResultCause getTestResultCause();

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
     * テストに失敗した動作を取得します。
     *
     * @return テストに失敗した動作
     */
    @Nullable
    Action<?> getFailedAction();
}
