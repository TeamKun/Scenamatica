package net.kunmc.lab.scenamatica.interfaces.context;

import net.kunmc.lab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import net.kunmc.lab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.WorldBean;
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
    @NotNull World createStage(WorldBean bean) throws StageCreateFailedException;

    /**
     * ステージをコピーします。
     *
     * @param originalName コピー元のワールド名
     * @return 生成したステージ
     */
    @NotNull World createStage(String originalName) throws StageCreateFailedException;

    /**
     * ステージを破棄します。
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
