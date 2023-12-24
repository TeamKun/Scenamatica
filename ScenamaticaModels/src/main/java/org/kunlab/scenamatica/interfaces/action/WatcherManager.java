package org.kunlab.scenamatica.interfaces.action;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

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
    List<WatchingEntry> registerWatchers(@NotNull Plugin plugin,
                                         @NotNull ScenarioEngine engine,
                                         @NotNull ScenarioFileStructure scenario,
                                         @NotNull List<? extends CompiledAction> watchers,
                                         @NotNull WatchType type);

    /**
     * 監視を登録します。
     *
     * @param plugin  監視するプラグイン
     * @param watcher 監視する動作
     * @param type    監視の種類
     * @return 監視の登録情報
     */
    WatchingEntry registerWatcher(@NotNull ScenarioEngine engine,
                                  @NotNull CompiledAction watcher,
                                  @NotNull ScenarioFileStructure scenario,
                                  @NotNull Plugin plugin,
                                  @NotNull WatchType type);

    /**
     * プラグインの全ての監視を解除します。
     *
     * @param plugin 監視を解除するプラグイン
     */
    void unregisterWatchers(@NotNull Plugin plugin);

    /**
     * プラグインの指定された種類の監視を解除します。
     *
     * @param plugin 監視を解除するプラグイン
     * @param type   監視を解除する種類
     */
    void unregisterWatchers(@NotNull Plugin plugin, @NotNull WatchType type);

    /**
     * 監視を解除します。
     *
     * @param entry 監視を解除する登録情報
     */
    void unregisterWatcher(@NotNull WatchingEntry entry);

    /**
     * 動作が実行されたときに呼び出されるメソッドです。
     */
    void onActionFired(@NotNull WatchingEntry entry, @NotNull Event event);
}
