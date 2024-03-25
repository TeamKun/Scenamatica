package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

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
    @Versioned(from = "1.16.5")
    void move(NMSMoveType moveType, Location location);

    /**
     * エンティティがアイテムをドロップします。
     *
     * @param stack   ドロップするアイテム
     * @param offsetY Y軸方向のオフセット
     * @return ドロップされたアイテム
     */
    NMSEntityItem dropItem(@NotNull NMSItemStack stack, float offsetY);
}
