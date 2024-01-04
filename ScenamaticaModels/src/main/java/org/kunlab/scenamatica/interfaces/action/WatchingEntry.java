package org.kunlab.scenamatica.interfaces.action;

import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

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
}
