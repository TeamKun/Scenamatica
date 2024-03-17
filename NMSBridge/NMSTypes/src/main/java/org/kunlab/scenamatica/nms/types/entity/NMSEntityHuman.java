package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.HumanEntity;

/**
 * {@link HumanEntity} のラッパです。
 */
public interface NMSEntityHuman extends NMSEntityLiving
{
    /**
     * ラップしている {@link HumanEntity} を取得します。
     *
     * @return {@link HumanEntity}
     */
    HumanEntity getBukkit();
}
