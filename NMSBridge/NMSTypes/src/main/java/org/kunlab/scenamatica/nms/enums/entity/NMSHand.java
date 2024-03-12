package org.kunlab.scenamatica.nms.enums.entity;

import org.bukkit.inventory.EquipmentSlot;
import org.kunlab.scenamatica.nms.NMSElement;

/**
 * エンティティの手です。
 */
public enum NMSHand implements NMSElement
{
    MAIN_HAND,
    OFF_HAND;

    public static NMSHand fromEquipmentSlot(EquipmentSlot slot)
    {
        if (slot == null)
            return null;

        switch (slot)
        {
            case HAND:
                return MAIN_HAND;
            case OFF_HAND:
                return OFF_HAND;
            default:
                throw new IllegalArgumentException("Unknown hand slot: " + slot);
        }
    }

    public EquipmentSlot toEquipmentSlot()
    {
        return this == MAIN_HAND ? EquipmentSlot.HAND: EquipmentSlot.OFF_HAND;
    }
}
