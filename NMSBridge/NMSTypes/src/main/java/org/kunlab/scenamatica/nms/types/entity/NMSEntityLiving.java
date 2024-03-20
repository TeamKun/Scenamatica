package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;

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
    @Versioned(from = "1.16.5")
    void consume(NMSHand hand);

    /**
     * プレイヤのアイテムのは回をブロードキャストします。
     *
     * @param slot スロット
     */
    @Versioned(from = "1.16.5")
    void broadcastItemBreak(NMSItemSlot slot);
}
