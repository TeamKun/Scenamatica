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
    void init();

    /**
     * アクションのコンパイラを取得します。
     *
     * @return アクションのコンパイラ
     */
    ActionCompiler getCompiler();

    /**
     * 動作の実行をキューに追加します。
     *
     * @param action 動作
     * @param <A>    動作の引数の型
     */
    <A extends ActionArgument> void queueExecute(@NotNull CompiledAction<A> action);

    /**
     * 動作の監視を追加します。
     *
     * @param plugin    監視するプラグイン
     * @param action    監視する動作
     * @param watchType 監視の種類です。
     * @param <A>       動作の引数の型
     */
    <A extends ActionArgument> void queueWatch(@NotNull Plugin plugin,
                                               @NotNull ScenarioEngine engine,
                                               @NotNull ScenarioFileStructure scenario,
                                               @NotNull CompiledAction<A> action,
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
