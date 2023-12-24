package org.kunlab.scenamatica.interfaces.action;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.List;

/**
 * 監視の登録情報を表します。
 */
public interface WatchingEntry
{
    /**
     * 監視を管理するマネージャーを取得します。
     *
     * @return 監視を管理するマネージャー
     */
    WatcherManager getManager();

    /**
     * シナリオエンジンを取得します。
     *
     * @return シナリオエンジン
     */
    ScenarioEngine getEngine();

    /**
     * 監視を行うプラグインを取得します。
     *
     * @return 監視を行うプラグイン
     */
    Plugin getPlugin();

    /**
     * 監視を行うシナリオを取得します。
     */
    ScenarioFileStructure getScenario();

    /**
     * 監視する動作を取得します。
     *
     * @return 監視する動作
     */
    CompiledAction getAction();

    /**
     * 監視の種類を取得します。
     *
     * @return 監視の種類
     */
    WatchType getType();

    /**
     * 監視に登録されたリスナーを取得します。
     *
     * @return 監視に登録されたリスナー
     */
    List<Pair<Class<? extends Event>, RegisteredListener>> getListeners();

    /**
     * 監視を登録します。
     *
     * @param eventType 監視するイベントのクラス
     * @return 監視に登録されたリスナー
     */
    RegisteredListener register(Class<? extends Event> eventType);

    /**
     * 監視を解除します。
     */
    void unregister();


}
