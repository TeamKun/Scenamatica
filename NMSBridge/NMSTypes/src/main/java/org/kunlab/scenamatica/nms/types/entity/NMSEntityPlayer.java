package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

/**
 * {@link Player} のラッパです。
 */
public interface NMSEntityPlayer extends NMSEntityHuman
{
    /**
     * ラップしている {@link Player} を取得します。
     *
     * @return {@link Player}
     */
    Player getBukkit();

    /**
     * インタラクトマネージャを取得します。
     *
     * @return インタラクトマネージャ
     */
    NMSPlayerInteractManager getInteractManager();
}
