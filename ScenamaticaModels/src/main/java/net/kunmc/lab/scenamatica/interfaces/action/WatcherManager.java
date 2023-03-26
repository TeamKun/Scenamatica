package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 動作の監視を管理します。
 */
public interface WatcherManager
{
    /**
     * 監視を登録します。
     *
     * @param plugin   監視するプラグイン
     * @param watchers 監視する動作のリスト
     * @param type     監視の種類
     * @return 監視の登録情報
     */
    List<WatchingEntry<?>> registerWatchers(@NotNull Plugin plugin,
                                            @NotNull ScenarioFileBean scenario,
                                            @NotNull List<? extends Pair<Action<?>, ActionArgument>> watchers,
                                            @NotNull WatchType type);

    /**
     * 監視を登録します。
     *
     * @param plugin  監視するプラグイン
     * @param watcher 監視する動作
     * @param type    監視の種類
     * @return 監視の登録情報
     */
    <A extends ActionArgument> WatchingEntry<A> registerWatcher(@NotNull Action<A> watcher,
                                                                @Nullable A argument,
                                                                @NotNull ScenarioFileBean scenario,
                                                                @NotNull Plugin plugin,
                                                                @NotNull WatchType type);

    /**
     * プラグインの全ての監視を解除します。
     *
     * @param plugin 監視を解除するプラグイン
     */
    void unregisterWatchers(@NotNull Plugin plugin);

    /**
     * 監視を解除します。
     *
     * @param entry 監視を解除する登録情報
     */
    void unregisterWatcher(@NotNull WatchingEntry<?> entry);

    /**
     * 動作が実行されたときに呼び出されるメソッドです。
     */
    void onActionFired(@NotNull WatchingEntry<?> entry);
}
