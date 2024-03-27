package org.kunlab.scenamatica.nms.types;

import org.bukkit.Server;
import org.kunlab.scenamatica.nms.NMSWrapped;

public interface NMSMinecraftServer extends NMSWrapped
{
    /**
     * プレイヤーリストを取得します。
     *
     * @return プレイヤーリスト
     */
    NMSPlayerList getPlayerList();

    @Override
    Server getBukkit();
}
