package org.kunlab.scenamatica.nms.types;

import org.bukkit.Server;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.supports.WorldMap;

public interface NMSMinecraftServer extends NMSWrapped
{
    // ====================[ FIELDS ]===================
    @Versioned(from = "1.16.5")
    WorldMap getWorlds();

    /**
     * プレイヤーリストを取得します。
     *
     * @return プレイヤーリスト
     */
    NMSPlayerList getPlayerList();

    @Override
    Server getBukkit();
}
