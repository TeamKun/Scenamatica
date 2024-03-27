package org.kunlab.scenamatica.interfaces.context;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.StageType;
import org.kunlab.scenamatica.exceptions.context.stage.StageAlreadyDestroyedException;

/**
 * シナリオのステージです。
 */
public interface Stage
{
    /**
     * 割り当てられたワールドを取得します。
     *
     * @return ワールド
     */
    @NotNull
    World getWorld();

    /**
     * ステージの種類を取得します。
     *
     * @return ステージの種類
     */
    @NotNull
    StageType getType();

    /**
     * ステージを破棄します。
     */
    void destroy() throws StageAlreadyDestroyedException;

    /**
     * ステージが破棄されているかどうかを取得します。
     *
     * @return 破棄されているかどうか
     */
    boolean isDestroyed();
}
