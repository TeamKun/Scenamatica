package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.ScenarioState;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

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
     * @param trigger シナリオを開始したトリガー
     * @return テスト結果
     */
    @NotNull
    ScenarioResult start(@NotNull TriggerBean trigger) throws TriggerNotFoundException;

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
    ScenarioFileBean getScenario();

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
    TriggerBean getRanBy();

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
    CompiledScenarioAction<?> getCurrentScenario();

    /**
     * シナリオの結果を受け取るオブジェクトを取得します。
     *
     * @return シナリオの結果を受け取るオブジェクト
     */
    ScenarioResultDeliverer getDeliverer();

    /**
     * コンパイルされたシナリオのアクションを取得します。
     *
     * @return コンパイルされたシナリオのアクション
     */
    List<CompiledScenarioAction<?>> getActions();

    /**
     * コンパイルされたトリガのアクションを取得します。
     *
     * @return コンパイルされたトリガのアクション
     */
    List<CompiledTriggerAction> getTriggerActions();
}
