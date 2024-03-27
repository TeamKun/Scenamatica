package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

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

    /**
     * 装備を取得します。
     *
     * @param slot 取得する装備のスロット
     * @return 装備
     */
    NMSItemStack getEquipment(NMSItemSlot slot);

    /**
     * アイテムをドロップします。
     *
     * @param stack        ドロップするアイテム
     * @param dropAll      すべてドロップするかどうか
     * @param copyUniqueID ユニークIDをコピーするかどうか
     * @return ドロップしたアイテム
     */
    @Nullable
    NMSEntityItem drop(@NotNull NMSItemStack stack, boolean dropAll, boolean copyUniqueID);

    /**
     * アイテムをドロップします。
     *
     * @param dropAll すべてドロップするかどうか
     * @return ドロップに成功したかどうか
     */
    boolean drop(boolean dropAll);

    default NMSEntityItem drop(NMSItemStack stack, boolean dropAll)
    {
        return this.drop(stack, dropAll, true);
    }

    /**
     * 食料レベルを取得します。
     *
     * @return 食料レベル
     */
    int getFoodLevel();

    /**
     * 食料レベルを設定します。
     *
     * @param foodLevel 食料レベル
     */
    void setFoodLevel(int foodLevel);
}
