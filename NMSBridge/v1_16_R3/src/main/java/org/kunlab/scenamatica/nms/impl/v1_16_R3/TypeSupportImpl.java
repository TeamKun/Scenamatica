package org.kunlab.scenamatica.nms.impl.v1_16_R3;

import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.TypeSupport;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;

public class TypeSupportImpl implements TypeSupport
{
    public static PacketPlayInUseEntity.EnumEntityUseAction toNMS(NMSEntityUseAction action)
    {
        switch (action)
        {
            case ATTACK:
                return PacketPlayInUseEntity.EnumEntityUseAction.ATTACK;
            case INTERACT:
                return PacketPlayInUseEntity.EnumEntityUseAction.INTERACT;
            case INTERACT_AT:
                return PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT;
            default:
                throw new IllegalArgumentException("Unknown NMSEntityUseAction: " + action.name());
        }
    }

    public static NMSEntityUseAction fromNMS(PacketPlayInUseEntity.EnumEntityUseAction action)
    {
        switch (action)
        {
            case ATTACK:
                return NMSEntityUseAction.ATTACK;
            case INTERACT:
                return NMSEntityUseAction.INTERACT;
            case INTERACT_AT:
                return NMSEntityUseAction.INTERACT_AT;
            default:
                throw new IllegalArgumentException("Unknown PacketPlayInUseEntity.EnumEntityUseAction: " + action.name());
        }
    }

    public static NMSHand fromNMS(EnumHand hand)
    {
        switch (hand)
        {
            case MAIN_HAND:
                return NMSHand.MAIN_HAND;
            case OFF_HAND:
                return NMSHand.OFF_HAND;
            default:
                throw new IllegalArgumentException("Unknown EnumHand: " + hand.name());
        }
    }

    public static EnumHand toNMS(NMSHand hand)
    {
        switch (hand)
        {
            case MAIN_HAND:
                return EnumHand.MAIN_HAND;
            case OFF_HAND:
                return EnumHand.OFF_HAND;
            default:
                throw new IllegalArgumentException("Unknown NMSHand: " + hand.name());
        }
    }

    public static EnumItemSlot toNMS(NMSItemSlot slot)
    {
        switch (slot)
        {
            case MAINHAND:
                return EnumItemSlot.MAINHAND;
            case OFFHAND:
                return EnumItemSlot.OFFHAND;
            case FEET:
                return EnumItemSlot.FEET;
            case LEGS:
                return EnumItemSlot.LEGS;
            case CHEST:
                return EnumItemSlot.CHEST;
            case HEAD:
                return EnumItemSlot.HEAD;
            default:
                throw new IllegalArgumentException("Unknown NMSItemSlot: " + slot.name());
        }
    }

    public static NMSItemSlot fromNMS(EnumItemSlot slot)
    {
        switch (slot)
        {
            case MAINHAND:
                return NMSItemSlot.MAINHAND;
            case OFFHAND:
                return NMSItemSlot.OFFHAND;
            case FEET:
                return NMSItemSlot.FEET;
            case LEGS:
                return NMSItemSlot.LEGS;
            case CHEST:
                return NMSItemSlot.CHEST;
            case HEAD:
                return NMSItemSlot.HEAD;
            default:
                throw new IllegalArgumentException("Unknown EnumItemSlot: " + slot.name());
        }
    }

    public static EnumMoveType toNMS(NMSMoveType moveType)
    {
        switch (moveType)
        {
            case SELF:
                return EnumMoveType.SELF;
            case PLAYER:
                return EnumMoveType.PLAYER;
            case PISTON:
                return EnumMoveType.PISTON;
            case SHULKER_BOX:
                return EnumMoveType.SHULKER_BOX;
            case SHULKER:
                return EnumMoveType.SHULKER;
            default:
                throw new IllegalArgumentException("Unknown NMSMoveType: " + moveType.name());
        }
    }

    public static NMSMoveType fromNMS(EnumMoveType moveType)
    {
        switch (moveType)
        {
            case SELF:
                return NMSMoveType.SELF;
            case PLAYER:
                return NMSMoveType.PLAYER;
            case PISTON:
                return NMSMoveType.PISTON;
            case SHULKER_BOX:
                return NMSMoveType.SHULKER_BOX;
            case SHULKER:
                return NMSMoveType.SHULKER;
            default:
                throw new IllegalArgumentException("Unknown EnumMoveType: " + moveType.name());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, U extends NMSElement> T toNMS(@Nullable U enumValue, @NotNull Class<T> clazz)
    {
        if (enumValue == null)
            return null;

        Object value = null;
        if (enumValue instanceof NMSEntityUseAction && clazz.equals(PacketPlayInUseEntity.EnumEntityUseAction.class))
            value = toNMS((NMSEntityUseAction) enumValue);
        else if (enumValue instanceof NMSHand && clazz.equals(EnumHand.class))
            value = toNMS((NMSHand) enumValue);
        else if (enumValue instanceof NMSItemSlot && clazz.equals(EnumItemSlot.class))
            value = toNMS((NMSItemSlot) enumValue);
        else if (enumValue instanceof NMSMoveType && clazz.equals(EnumMoveType.class))
            value = toNMS((NMSMoveType) enumValue);

        if (value != null)
            return (T) value;

        throw new IllegalArgumentException("Unknown NMSElement: " + enumValue.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public <T extends NMSElement> T fromNMS(@Nullable Object nmsValue, @NotNull Class<T> clazz)
    {
        if (nmsValue == null)
            return null;

        Object value = null;
        if (nmsValue instanceof PacketPlayInUseEntity.EnumEntityUseAction)
            value = fromNMS((PacketPlayInUseEntity.EnumEntityUseAction) nmsValue);
        else if (nmsValue instanceof EnumHand)
            value = fromNMS((EnumHand) nmsValue);
        else if (nmsValue instanceof EnumItemSlot)
            value = fromNMS((EnumItemSlot) nmsValue);
        else if (nmsValue instanceof EnumMoveType)
            value = fromNMS((EnumMoveType) nmsValue);

        if (value != null)
            return (T) value;

        throw new IllegalArgumentException("Unknown NMSElement: " + nmsValue.getClass().getName());
    }
}
