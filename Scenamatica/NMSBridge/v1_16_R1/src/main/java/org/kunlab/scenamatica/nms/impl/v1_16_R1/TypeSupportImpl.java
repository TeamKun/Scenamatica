package org.kunlab.scenamatica.nms.impl.v1_16_R1;

import net.minecraft.server.v1_16_R1.EnumDirection;
import net.minecraft.server.v1_16_R1.EnumHand;
import net.minecraft.server.v1_16_R1.EnumItemSlot;
import net.minecraft.server.v1_16_R1.EnumMoveType;
import net.minecraft.server.v1_16_R1.PacketPlayInUseEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.TypeSupport;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.enums.voxel.NMSDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TypeSupportImpl implements TypeSupport
{
    private static final List<ConversionPair<?, ?>> CONVERSION_PAIRS = new ArrayList<>();

    static
    {
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSEntityUseAction.class, PacketPlayInUseEntity.EnumEntityUseAction.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSHand.class, EnumHand.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSItemSlot.class, EnumItemSlot.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSMoveType.class, EnumMoveType.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSDirection.class, EnumDirection.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
    }

    // <editor-fold desc="Conversion Methods">

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

    public static EnumDirection toNMS(NMSDirection direction)
    {
        switch (direction)
        {
            case NORTH:
                return EnumDirection.NORTH;
            case SOUTH:
                return EnumDirection.SOUTH;
            case WEST:
                return EnumDirection.WEST;
            case EAST:
                return EnumDirection.EAST;
            case UP:
                return EnumDirection.UP;
            case DOWN:
                return EnumDirection.DOWN;
            default:
                throw new IllegalArgumentException("Unknown NMSDirection: " + direction.name());
        }
    }

    public static NMSDirection fromNMS(EnumDirection direction)
    {
        switch (direction)
        {
            case NORTH:
                return NMSDirection.NORTH;
            case SOUTH:
                return NMSDirection.SOUTH;
            case WEST:
                return NMSDirection.WEST;
            case EAST:
                return NMSDirection.EAST;
            case UP:
                return NMSDirection.UP;
            case DOWN:
                return NMSDirection.DOWN;
            default:
                throw new IllegalArgumentException("Unknown EnumDirection: " + direction.name());
        }
    }

    // </editor-fold>

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, U extends NMSElement> T toNMS(@Nullable U enumValue, @NotNull Class<T> clazz)
    {
        if (enumValue == null)
            return null;

        for (ConversionPair pair : CONVERSION_PAIRS)
            if (pair.nmsType.isInstance(enumValue))
                return (T) pair.toNmsFunction.apply(enumValue);

        throw new IllegalArgumentException("Unknown NMSElement: " + enumValue.getClass().getName());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends NMSElement> T fromNMS(@Nullable Object nmsValue, @NotNull Class<T> clazz)
    {
        if (nmsValue == null)
            return null;

        for (ConversionPair pair : CONVERSION_PAIRS)
            if (pair.nmsClass.isInstance(nmsValue))
                return (T) pair.fromNmsFunction.apply(nmsValue);

        throw new IllegalArgumentException("Unknown NMSElement: " + nmsValue.getClass().getName());
    }

    private static class ConversionPair<U extends NMSElement, T>
    {
        private final Class<U> nmsType;
        private final Class<T> nmsClass;
        private final Function<U, T> toNmsFunction;
        private final Function<T, U> fromNmsFunction;

        private ConversionPair(Class<U> nmsType, Class<T> nmsClass, Function<U, T> toNmsFunction, Function<T, U> fromNmsFunction)
        {
            this.nmsType = nmsType;
            this.nmsClass = nmsClass;
            this.toNmsFunction = toNmsFunction;
            this.fromNmsFunction = fromNmsFunction;
        }
    }
}
