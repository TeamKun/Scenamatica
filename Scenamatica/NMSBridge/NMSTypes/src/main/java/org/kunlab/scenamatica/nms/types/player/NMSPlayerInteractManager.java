package org.kunlab.scenamatica.nms.types.player;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.types.block.NMSBlockPosition;

public interface NMSPlayerInteractManager extends NMSWrapped
{
    /**
     * ブロックを破壊します。
     *
     * @param position 破壊するブロックの位置
     */
    void breakBlock(@NotNull NMSBlockPosition position);

    @Override
    default Object getBukkit()
    {
        throw new UnsupportedOperationException();
    }
}
