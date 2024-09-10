package org.kunlab.scenamatica.nms.impl.v1_18_R1;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.TypeSupport;
import org.kunlab.scenamatica.nms.enums.NMSHand;
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
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSHand.class, InteractionHand.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSItemSlot.class, EquipmentSlot.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSMoveType.class, MoverType.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSDirection.class, Direction.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
    }

    // <editor-fold desc="Conversion Methods">

    public static NMSHand fromNMS(InteractionHand hand)
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

    public static InteractionHand toNMS(NMSHand hand)
    {
        switch (hand)
        {
            case MAIN_HAND:
                return InteractionHand.MAIN_HAND;
            case OFF_HAND:
                return InteractionHand.OFF_HAND;
            default:
                throw new IllegalArgumentException("Unknown NMSHand: " + hand.name());
        }
    }

    public static EquipmentSlot toNMS(NMSItemSlot slot)
    {
        switch (slot)
        {
            case MAINHAND:
                return EquipmentSlot.MAINHAND;
            case OFFHAND:
                return EquipmentSlot.OFFHAND;
            case FEET:
                return EquipmentSlot.FEET;
            case LEGS:
                return EquipmentSlot.LEGS;
            case CHEST:
                return EquipmentSlot.CHEST;
            case HEAD:
                return EquipmentSlot.HEAD;
            default:
                throw new IllegalArgumentException("Unknown NMSItemSlot: " + slot.name());
        }
    }

    public static NMSItemSlot fromNMS(EquipmentSlot slot)
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

    public static MoverType toNMS(NMSMoveType moveType)
    {
        switch (moveType)
        {
            case SELF:
                return MoverType.SELF;
            case PLAYER:
                return MoverType.PLAYER;
            case PISTON:
                return MoverType.PISTON;
            case SHULKER_BOX:
                return MoverType.SHULKER_BOX;
            case SHULKER:
                return MoverType.SHULKER;
            default:
                throw new IllegalArgumentException("Unknown NMSMoveType: " + moveType.name());
        }
    }

    public static NMSMoveType fromNMS(MoverType moveType)
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

    public static Direction toNMS(NMSDirection direction)
    {
        switch (direction)
        {
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
            case EAST:
                return Direction.EAST;
            case UP:
                return Direction.UP;
            case DOWN:
                return Direction.DOWN;
            default:
                throw new IllegalArgumentException("Unknown NMSDirection: " + direction.name());
        }
    }

    public static NMSDirection fromNMS(Direction direction)
    {
        switch (direction)
        {
            case DOWN:
                return NMSDirection.DOWN;
            case UP:
                return NMSDirection.UP;
            case NORTH:
                return NMSDirection.NORTH;
            case SOUTH:
                return NMSDirection.SOUTH;
            case WEST:
                return NMSDirection.WEST;
            case EAST:
                return NMSDirection.EAST;
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
