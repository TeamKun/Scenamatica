package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.NMSWrapped;
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
    void move(NMSMoveType moveType, Location location);

    /**
     * エンティティがアイテムをドロップします。
     *
     * @param stack   ドロップするアイテム
     * @param offsetY Y軸方向のオフセット
     * @return ドロップされたアイテム
     */
    NMSEntityItem dropItem(@NotNull NMSItemStack stack, float offsetY);

    /**
     * エンティティにダメージを与えます。
     * a.k.a. <code>Entity#hurt(DamageSource, float)</code>
     *
     * @param source ダメージのソース
     * @param damage ダメージ量
     * @return ダメージを受けたかどうか
     */
    boolean damageEntity(NMSDamageSource source, float damage);

    /**
     * エンティティが非表示かどうかを取得します。
     *
     * @return 非表示かどうか
     */
    boolean isInvisible();

    /**
     * エンティティを非表示にします。
     *
     * @param invisible 非表示にするかどうか
     */
    void setInvisible(boolean invisible);
}
