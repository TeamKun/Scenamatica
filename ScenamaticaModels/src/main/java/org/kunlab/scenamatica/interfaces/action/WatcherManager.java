package org.kunlab.scenamatica.interfaces.action;

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
     * @param watchers 監視する動作のリスト
     * @param type     監視の種類
     */
    void registerWatchers(@NotNull ScenarioEngine engine,
                          @NotNull ScenarioFileStructure scenario,
                          @NotNull List<? extends CompiledAction> watchers,
                          @NotNull WatchType type);

    /**
     * 監視を登録します。
     *
     * @param watcher 監視する動作
     * @param type    監視の種類
     */
    void registerWatcher(@NotNull ScenarioEngine engine,
                         @NotNull CompiledAction watcher,
                         @NotNull ScenarioFileStructure scenario,
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
}
