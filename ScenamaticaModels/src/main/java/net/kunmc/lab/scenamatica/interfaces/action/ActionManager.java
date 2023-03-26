package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 動作の実行と監視を管理するクラスのインターフェースです。
 */
public interface ActionManager
{
    /**
     * 初期化します。
     */
    void init();

    /**
     * 動作の実行をキューに追加します。
     *
     * @param action   動作
     * @param argument 動作の引数
     * @param <A>      動作の引数の型
     */
    <A extends ActionArgument> void queueExecute(@NotNull Action<A> action, @Nullable A argument);

    /**
     * 動作の監視を追加します。
     *
     * @param plugin    監視するプラグイン
     * @param action    監視する動作
     * @param watchType 監視の種類です。
     * @param argument  動作の引数
     * @param <A>       動作の引数の型
     */
    <A extends ActionArgument> void queueWatch(@NotNull Plugin plugin,
                                               @NotNull ScenarioFileBean scenario,
                                               @NotNull Action<A> action,
                                               @NotNull WatchType watchType,
                                               @Nullable A argument);

    /**
     * シナリオをスタートします。
     * このメソッドは, 内部で実行と監視を振り分けそれぞれをキューに追加します。
     *
     * @param scenario シナリオ
     * @see #queueExecute(Action, ActionArgument)
     * @see #queueWatch(Plugin, ScenarioFileBean, Action, WatchType, ActionArgument)
     */
    void startScenario(@NotNull ScenarioFileBean scenario);

    /**
     * シャットダウンします。
     */
    void shutdown();

    WatcherManager getWatcherManager();
}
