package org.kunlab.scenamatica.interfaces.context;

import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageBean;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * シナリオを実行するステージを管理するインターフェースです。
 */
public interface StageManager
{
    /**
     * ステージを生成します。
     *
     * @param bean ステージ情報
     * @return 生成したステージ
     */
    @NotNull World createStage(StageBean bean) throws StageCreateFailedException;

    /**
     * ステージをコピーします。
     *
     * @param originalName コピー元のワールド名
     * @return 生成したステージ
     */
    @NotNull World createStage(String originalName) throws StageCreateFailedException;

    /**
     * 共有ステージを設定します。
     *
     * @param name 名前
     */
    @NotNull
    World shared(@NotNull String name);

    /**
     * ステージを破棄します。
     * 共有ステージの場合は破棄されません。
     */
    void destroyStage() throws StageNotCreatedException;

    /**
     * ステージを取得します。
     *
     * @return ステージ
     */
    World getStage();

    /**
     * ステージが生成されているかどうかを取得します。
     *
     * @return 生成されている場合は true
     */
    boolean isStageCreated();

    /**
     * ステージが既存のワールドからコピーされたものかどうかを取得します。
     *
     * @return コピーされたものの場合は true
     */
    boolean hasCopied();
}
