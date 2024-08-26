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
    void consume(NMSHand hand);

    /**
     * プレイヤのアイテムのは回をブロードキャストします。
     *
     * @param slot スロット
     */
    void broadcastItemBreak(NMSItemSlot slot);

    /**
     * エンティティをを受け取ります。
     *
     * @param entity 受け取るエンティティ
     * @param amount 受け取る量
     */
    void receive(NMSEntity entity, int amount);

    /**
     * エンティティが寝ているかどうかを取得します。
     *
     * @return 寝ているかどうか
     */
    boolean isSleeping();

    /**
     * 体にぶっ刺さっている矢の数を取得します。
     *
     * @return 矢の数
     */
    int getArrowCount();

    /**
     * 体にぶっ刺さっている矢の数を設定します。
     *
     * @param count 矢の数
     */
    void setArrowCount(int count);

    /**
     * 矢のクールダウンを取得します。
     *
     * @return クールダウン
     */
    @Versioned(from = "1.13.2")
    int getArrowCooldown();

    /**
     * 矢のクールダウンを設定します。
     *
     * @param cooldown クールダウン
     */
    @Versioned(from = "1.13.2")
    void setArrowCooldown(int cooldown);
}
