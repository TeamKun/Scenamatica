package org.kunlab.scenamatica.interfaces.context;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.UUID;

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
    Context prepareContext(@NotNull ScenarioFileStructure scenario, @NotNull UUID testID) throws StageCreateFailedException, StageNotCreatedException;

    /**
     * 環境を破棄します。
     */
    void destroyContext();

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
