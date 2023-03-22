package net.kunmc.lab.scenamatica.interfaces.context;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;

/**
 * シナリオの実行に必要な環境等を管理するインターフェースです。
 */
public interface ContextManager
{
    /**
     * 環境を準備します。
     *
     * @param scenario シナリオファイル
     * @return 成功した場合は true
     */
    boolean prepareContext(ScenarioFileBean scenario);

    /**
     * 環境を破棄します。
     */
    void shutdown();

    /**
     * アクターマネージャーを取得します。
     *
     * @return アクターマネージャー
     */
    ActorManager getActorManager();

    /**
     * ステージマネージャーを取得します。
     *
     * @return ステージマネージャー
     */
    StageManager getStageManager();
}
