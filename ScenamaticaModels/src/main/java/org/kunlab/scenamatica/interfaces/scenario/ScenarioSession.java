package org.kunlab.scenamatica.interfaces.scenario;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.List;
import java.util.function.Consumer;

/**
 * シナリオセッションを表すインターフェースです。
 */
public interface ScenarioSession
{
    /**
     * セッションにシナリオを追加します。
     *
     * @param engine   シナリオエンジン
     * @param trigger  トリガー
     * @param callback コールバック
     */
    void add(@NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger, @Nullable Consumer<? super ScenarioResult> callback);

    /**
     * セッションにシナリオを追加します。
     *
     * @param engine  シナリオエンジン
     * @param trigger トリガー
     */
    default void add(@NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger)
    {
        this.add(engine, trigger, null);
    }

    /**
     * セッションからシナリオを削除します。
     *
     * @param plugin プラグイン
     * @throws IllegalStateException 実行が完了した後に呼び出された場合
     */
    void remove(Plugin plugin);

    /**
     * セッションからシナリオを削除します。
     *
     * @param plugin プラグイン
     * @param name   シナリオ名
     * @throws IllegalStateException 実行が完了した後に呼び出された場合
     */
    void remove(Plugin plugin, String name);

    /**
     * セッションに登録されているシナリオを取得します。
     * 注：このメソッドより返されるリストは不変です。
     *
     * @return シナリオの不変リスト
     */
    List<QueuedScenario> getScenarios();

    /**
     * セッションが差k末井された時刻を取得します。
     *
     * @return 作成時刻
     */
    long getCreatedAt();

    /**
     * セッションが開始された時刻を取得します。
     *
     * @return 開始時刻
     * @throws IllegalStateException セッションが開始されていない場合
     */
    long getStartedAt();

    /**
     * セッションが実行中かどうかを取得します。
     *
     * @return 実行中かどうか
     */
    boolean isRunning();

    /**
     * セッションが終了した時刻を取得します。
     *
     * @return 終了時刻
     * @throws IllegalStateException セッションが実行中の場合
     */
    long getFinishedAt();
}
