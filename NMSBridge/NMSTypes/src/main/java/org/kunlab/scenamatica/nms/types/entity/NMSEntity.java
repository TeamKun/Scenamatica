package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;
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
    Entity getBukkit();

    /**
     * エンティティを動かします。
     *
     * @param moveType 動かす方法
     * @param location 動かす先の座標
     */
    @Versioned(from = "1.13")
    void move(NMSMoveType moveType, Location location);
}
