package org.kunlab.scenamatica.interfaces.action;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

/**
 * 動作の実行と監視を管理するクラスのインターフェースです。
 */
public interface ActionRunManager
{
    /**
     * 初期化します。
     */
    void init(@NotNull Plugin scenamatica);

    /**
     * 動作の実行をキューに追加します。
     *
     * @param action 動作
     */
    void queueExecute(@NotNull CompiledAction action);

    /**
     * 動作の監視を追加します。
     *
     * @param engine    シナリオエンジン
     * @param scenario  シナリオファイル構造
     * @param action    監視する動作
     * @param watchType 監視の種類です。
     */
    void queueWatch(@NotNull ScenarioEngine engine,
                    @NotNull ScenarioFileStructure scenario,
                    @NotNull CompiledAction action,
                    @NotNull WatchType watchType);

    /**
     * シャットダウンします。
     */
    void shutdown();

    /**
     * 監視を管理するクラスを取得します。
     *
     * @return 監視を管理するクラス
     */
    WatcherManager getWatcherManager();
}
