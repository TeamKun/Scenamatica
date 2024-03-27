package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.Item;

/**
 * Item のラッパです。
 */
public interface NMSEntityItem extends NMSEntity
{
    @Override
    Item getBukkit();
}
