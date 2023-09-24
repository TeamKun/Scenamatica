package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.nms.AvailableOn;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;

/**
 * {@link Entity} のラッパです。
 */
public interface NMSEntity extends NMSWrapped
{
    /**
     * ラップしている {@link Entity} を取得します。
     *
     * @return {@link Entity}
     */
    Entity getBukkitEntity();

    /**
     * エンティティを動かします。
     *
     * @param moveType 動かす方法
     * @param location 動かす先の座標
     */
    @AvailableOn("1.16.5")
    void move(NMSMoveType moveType, Location location);
}
