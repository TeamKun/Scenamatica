package org.kunlab.scenamatica.nms.types;

import org.bukkit.Server;
import org.kunlab.scenamatica.nms.NMSWrapped;

public interface NMSMinecraftServer extends NMSWrapped
{
    /**
     * プレイヤリストを取得します。
     *
     * @return プレイヤリスト
     */
    NMSPlayerList getPlayerList();

    @Override
    Server getBukkit();
}
