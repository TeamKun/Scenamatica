package org.kunlab.scenamatica.nms.enums.entity;

import org.bukkit.inventory.EquipmentSlot;
import org.kunlab.scenamatica.nms.NMSElement;

public enum NMSItemSlot implements NMSElement
{
    MAINHAND,
    OFFHAND,
    FEET,
    LEGS,
    CHEST,
    HEAD;

    public static NMSItemSlot fromBukkit(EquipmentSlot slot)
    {
        if (slot == null)
            return null;

        switch (slot)
        {
            case HAND:
                return MAINHAND;
            case OFF_HAND:
                return OFFHAND;
            case FEET:
                return FEET;
            case LEGS:
                return LEGS;
            case CHEST:
                return CHEST;
            case HEAD:
                return HEAD;
            default:
                throw new IllegalArgumentException("Unknown item slot: " + slot);
        }
    }

    public EquipmentSlot toBukkit()
    {
        switch (this)
        {
            case MAINHAND:
                return EquipmentSlot.HAND;
            case OFFHAND:
                return EquipmentSlot.OFF_HAND;
            case FEET:
                return EquipmentSlot.FEET;
            case LEGS:
                return EquipmentSlot.LEGS;
            case CHEST:
                return EquipmentSlot.CHEST;
            case HEAD:
                return EquipmentSlot.HEAD;
            default:
                throw new IllegalArgumentException("Unknown item slot: " + this.name());
        }
    }
}
