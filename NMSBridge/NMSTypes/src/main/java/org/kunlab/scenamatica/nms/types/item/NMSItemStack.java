package org.kunlab.scenamatica.nms.types.item;

import org.bukkit.inventory.ItemStack;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;

import java.util.function.Consumer;

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
     * @param damage  ダメージ
     * @param owner   アイテムの所有者
     * @param onBreak アイテムが壊れたときの処理
     * @param <T>     所有者の型
     */
    @Versioned(from = "1.16.5") <T extends NMSEntityLiving> void damage(int damage, T owner, Consumer<T> onBreak);

    /**
     * ラップしている {@link NMSItem} を取得します。
     *
     * @return {@link NMSItem}
     */
    @Versioned(from = "1.16.5") NMSItem getItem();
}
