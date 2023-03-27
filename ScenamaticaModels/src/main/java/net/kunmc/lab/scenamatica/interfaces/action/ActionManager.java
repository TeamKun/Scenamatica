package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.apache.logging.log4j.util.BiConsumer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
     * アクションのコンパイラを取得します。
     *
     * @return アクションのコンパイラ
     */
    ActionCompiler getCompiler();

    /**
     * 動作の実行をキューに追加します。
     *
     * @param action      動作
     * @param argument    動作の引数
     * @param onException 動作の実行に失敗したときに呼び出されるコールバック
     * @param onSuccess   動作の実行に成功したときに呼び出されるコールバック
     * @param <A>         動作の引数の型
     */
    <A extends ActionArgument> CompiledAction<A> queueExecute(@NotNull Action<A> action,
                                                              @Nullable A argument,
                                                              @NotNull BiConsumer<CompiledAction<A>, Throwable> onException,
                                                              @Nullable Consumer<CompiledAction<A>> onSuccess);

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
