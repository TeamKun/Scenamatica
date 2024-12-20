package org.kunlab.scenamatica.nms.types.item;

import org.bukkit.inventory.ItemStack;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;

/**
 * ItemStack の NMS インターフェースです。
 */
public interface NMSItemStack extends NMSWrapped
{
    /**
     * ラップしている {@link ItemStack} を取得します。
     *
     * @return {@link ItemStack}
     */
    ItemStack getBukkit();

    /**
     * アイテムにダメージを与えます。
     *
     * @param <T>    所有者の型
     * @param damage ダメージ
     * @param owner  アイテムの所有者
     */
    @Versioned(to = "1.19.3")
    default <T extends NMSEntityLiving> void damage(int damage, T owner)
    {
        this.damage(damage, owner, null);
    }

    /**
     * アイテムをダメージを与えます。
     *
     * @param <T>    所有者の型
     * @param damage ダメージ
     * @param owner  アイテムの所有者
     * @param slot  装備スロット
     */
    @Versioned(from = "1.20")
    <T extends NMSEntityLiving> void damage(int damage, T owner, NMSItemSlot slot);

    /**
     * ラップしている {@link NMSItem} を取得します。
     *
     * @return {@link NMSItem}
     */
    NMSItem getItem();
}
