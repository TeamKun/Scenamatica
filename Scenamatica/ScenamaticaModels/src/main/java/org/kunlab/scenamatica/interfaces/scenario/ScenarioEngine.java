package org.kunlab.scenamatica.interfaces.scenario;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;

import java.util.List;
import java.util.UUID;

/**
 * シナリオエンジンのインターフェースです。
 */
public interface ScenarioEngine
{
    /**
     * シナリオエンジンのマネージャを取得します。
     *
     * @return シナリオエンジンのマネージャ
     */
    @NotNull
    ScenarioManager getManager();

    /**
     * コンテキストを取得します。
     *
     * @return ステージ
     */
    @NotNull
    Context getContext();

    /**
     * シナリオを開始します。
     *
     * @param trigger        シナリオを開始したトリガー
     * @param variable       セッション変数
     * @param attemptedCount 試行された回数（n回目の実行）
     * @return テスト結果
     */
    @NotNull
    ScenarioResult start(@NotNull TriggerStructure trigger, @NotNull SessionStorage variable, int attemptedCount) throws TriggerNotFoundException;

    /**
     * シナリオを開始します。
     *
     * @param trigger  シナリオを開始したトリガー
     * @param variable セッション変数
     * @return テスト結果
     */
    @NotNull
    default ScenarioResult start(@NotNull TriggerStructure trigger, @NotNull SessionStorage variable) throws TriggerNotFoundException
    {
        return this.start(trigger, variable, 1);
    }

    /**
     * シナリオの実行をキャンセルします。
     */
    void cancel();

    /**
     * 対象のプラグインを取得します。
     *
     * @return 対象のプラグイン
     */
    Plugin getPlugin();

    /**
     * 実行するシナリオを取得します。
     *
     * @return シナリオ
     */
    ScenarioFileStructure getScenario();

    /**
     * シナリオの結果を受け取るリスナーを取得します。
     *
     * @return リスナー
     */
    ScenarioActionListener getListener();

    /**
     * シナリオが実行中かどうかを取得します。
     *
     * @return 実行中かどうか
     */
    boolean isRunning();

    /**
     * サーバのチックごとに呼び出されます。
     */
    void onTick();

    /**
     * シナリオを実行したトリガを取得します。
     *
     * @return トリガ
     */
    TriggerStructure getRanBy();

    /**
     * 与えられた一意のテスト ID を取得します。
     *
     * @return テスト ID
     */
    UUID getTestID();

    /**
     * シナリオが開始された時間を取得します。
     *
     * @return 開始時間
     */
    long getStartedAt();

    /**
     * ログの接頭辞を取得します。
     *
     * @return ログの接頭辞
     */
    String getLogPrefix();

    /**
     * シナリオが自動実行されたかどうかを取得します。
     *
     * @return 自動実行されたかどうか
     */
    boolean isAutoRun();

    /**
     * シナリオの状態を取得します。
     *
     * @return シナリオの状態
     */
    ScenarioState getState();

    /**
     * コンパイルされたシナリオを取得します。
     *
     * @return コンパイルされたシナリオ
     */
    CompiledScenarioAction getCurrentScenario();

    /**
     * シナリオの入力の参照を解放します。
     */
    void releaseScenarioInputs();

    /**
     * シナリオの結果を受け取るオブジェクトを取得します。
     *
     * @return シナリオの結果を受け取るオブジェクト
     */
    ActionResultDeliverer getDeliverer();

    /**
     * コンパイルされたシナリオのアクションを取得します。
     *
     * @return コンパイルされたシナリオのアクション
     */
    List<CompiledScenarioAction> getActions();

    /**
     * コンパイルされたトリガのアクションを取得します。
     *
     * @return コンパイルされたトリガのアクション
     */
    List<CompiledTriggerAction> getTriggerActions();

    /**
     * シナリオの実行を取得します。
     *
     * @return シナリオの実行
     */
    ScenarioExecutor getExecutor();
}
