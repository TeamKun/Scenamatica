package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.enums.entity.NMSHand;

/**
 * {@link LivingEntity} のラッパです。
 */
public interface NMSEntityLiving extends NMSEntity
{
    /**
     * ラップしている {@link LivingEntity} を取得します。
     *
     * @return {@link LivingEntity}
     */
    LivingEntity getBukkit();

    /**
     * 手に持っているアイテムを消費します。
     *
     * @param hand 手
     */
    void consume(NMSHand hand);
}
