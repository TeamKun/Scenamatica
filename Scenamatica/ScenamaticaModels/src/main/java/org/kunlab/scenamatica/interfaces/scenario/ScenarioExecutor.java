package org.kunlab.scenamatica.interfaces.scenario;

import org.bukkit.plugin.Plugin;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;

import java.util.Map;
import java.util.UUID;

/**
 * シナリオの実行を管理するインターフェースです。
 */
public interface ScenarioExecutor
{
    /**
     * エンジンを取得します。
     *
     * @return エンジン
     */
    ScenarioEngine getEngine();

    /**
     * 変数を取得します。
     *
     * @return 変数
     */
    SessionStorage getVariable();

    /**
     * プラグインを取得します。
     *
     * @return プラグイン
     */
    Plugin getPlugin();

    /**
     * シナリオのIDを取得します。
     *
     * @return シナリオのID
     */
    UUID getTestID();

    /**
     * シナリオの開始時刻を取得します。
     *
     * @return シナリオの開始時刻
     */
    long getStartedAt();

    /**
     * シナリオの終了時刻を取得します。
     *
     * @return シナリオの終了時刻
     */
    String getLogPrefix();

    /**
     * シナリオの終了時刻を取得します。
     *
     * @return シナリオの終了時刻
     */
    int getAttemptedCount();

    /**
     * 状態を取得します。
     *
     * @return 状態
     */
    ScenarioState getState();

    /**
     * 現在経過しているチック数を取得します。
     *
     * @return 経過しているチック数
     */
    long getElapsedTicks();

    /**
     * 現在のシナリオの実行を取得します。
     *
     * @return 現在のシナリオの実行
     */
    CompiledScenarioAction getCurrentScenario();

    /**
     * シナリオの入力を解決します。
     *
     * @param action 入力を解決するアクション
     */
    void resolveInputs(CompiledAction action);

    /**
     * 出力をアップロードします。
     *
     * @param sender  アクションの実行者
     * @param outputs 出力
     */
    void uploadScenarioOutputs(ActionContext sender, Map<String, Object> outputs);
}
