package org.kunlab.scenamatica.nms.types.block;

import org.bukkit.Location;
import org.kunlab.scenamatica.nms.NMSWrapped;

/**
 * NMS のブロックの位置を表します。
 */
public interface NMSBlockPosition extends NMSWrapped
{
    /**
     * この位置の X 座標を取得します。
     */
    int getX();

    /**
     * この位置の Y 座標を取得します。
     */
    int getY();

    /**
     * この位置の Z 座標を取得します。
     */
    int getZ();

    @Override
    Location getBukkit();
}
