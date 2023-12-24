package org.kunlab.scenamatica.interfaces.scenario;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.function.Consumer;

/**
 * 実行キューに登録されたシナリオを表すインターフェースです。
 */
public interface QueuedScenario
{
    /**
     * キューに登録された時刻を取得します。
     *
     * @return キューに登録された時刻
     */
    long getQueuedAt();

    /**
     * 対象のシナリオエンジンを取得します。
     *
     * @return シナリオエンジン
     */
    @NotNull
    ScenarioEngine getEngine();

    /**
     * 対象のトリガーを取得します。
     *
     * @return トリガー
     */
    @NotNull
    TriggerStructure getTrigger();

    /**
     * シナリオの実行結果を受け取るコールバックを取得します。
     *
     * @return コールバック
     */
    @Nullable
    Consumer<? super ScenarioResult> getCallback();

    /**
     * シナリオの実行を開始した時刻を取得します。
     *
     * @return シナリオの実行を開始した時刻
     * @throws IllegalStateException シナリオが実行中の場合
     */
    long getStartedAt();

    /**
     * シナリオが実行中かどうかを取得します。
     *
     * @return シナリオが実行中かどうか
     */
    boolean isRunning();

    /**
     * シナリオの実行結果を取得します。
     *
     * @return シナリオの実行結果
     * @throws IllegalStateException シナリオが実行中の場合
     * @see #isRunning()
     */
    ScenarioResult getResult();

    /**
     * シナリオが終了していた場合に、その時刻を取得します。
     *
     * @return シナリオが終了していた場合に、その時刻
     * @throws IllegalStateException シナリオが実行中の場合
     * @see #isRunning()
     */
    long getFinishedAt();

    /**
     * シナリオを実行します。
     *
     * @param variables セッション変数
     * @return シナリオの実行結果
     * @throws IllegalStateException シナリオが実行中の場合
     */
    ScenarioResult run(SessionVariableHolder variables) throws TriggerNotFoundException;

    /**
     * 最大リトライ回数を取得します。
     *
     * @return 最大リトライ回数
     */
    int getMaxAttemptCount();

    /**
     * リトライ回数を取得します。
     *
     * @return リトライ回数
     */
    int getAttemptCount();

    /**
     * 再試行のために、現在のシナリオをリセットします。
     * リセットカウントはインクリメントされます。
     *
     * @throws IllegalStateException シナリオが実行中の場合
     */
    void resetForRetry();
}
