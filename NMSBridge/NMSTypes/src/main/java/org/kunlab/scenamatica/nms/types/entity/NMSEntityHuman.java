package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.HumanEntity;
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

    NMSItemStack getEquipment(NMSItemSlot slot);
}
