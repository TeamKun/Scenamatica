package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.LivingEntity;

/**
 * {@link LivingEntity} のラッパです。
 */
public interface NMSLivingEntity extends NMSEntity
{
    /**
     * ラップしている {@link LivingEntity} を取得します。
     *
     * @return {@link LivingEntity}
     */
    LivingEntity getBukkitEntity();
}
