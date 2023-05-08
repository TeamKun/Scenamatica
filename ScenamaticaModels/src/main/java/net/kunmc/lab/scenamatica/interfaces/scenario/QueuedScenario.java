package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    TriggerBean getTrigger();

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
     * @throws IllegalStateException シナリオが実行中の場合
     */
    ScenarioResult run() throws TriggerNotFoundException;
}
