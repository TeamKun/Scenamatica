package org.kunlab.scenamatica.nms.types.item;

import org.bukkit.inventory.ItemStack;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;
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
     * アイテムをダメージを与えます。
     *
     * @param <T>    所有者の型
     * @param damage ダメージ
     * @param owner  アイテムの所有者
     */
    <T extends NMSEntityLiving> @Versioned void damage(int damage, T owner);

    /**
     * ラップしている {@link NMSItem} を取得します。
     *
     * @return {@link NMSItem}
     */
    @Versioned(from = "1.13.2")
    NMSItem getItem();
}
