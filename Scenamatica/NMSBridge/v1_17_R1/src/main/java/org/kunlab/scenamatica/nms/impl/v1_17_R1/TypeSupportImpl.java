package org.kunlab.scenamatica.nms.impl.v1_17_R1;

import net.minecraft.core.EnumDirection;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMoveType;
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
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSHand.class, EnumHand.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSItemSlot.class, EnumItemSlot.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSMoveType.class, EnumMoveType.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
        CONVERSION_PAIRS.add(new ConversionPair<>(NMSDirection.class, EnumDirection.class, TypeSupportImpl::toNMS, TypeSupportImpl::fromNMS));
    }

    // <editor-fold desc="Conversion Methods">

    public static NMSHand fromNMS(EnumHand hand)
    {
        switch (hand)
        {
            case a:
                return NMSHand.MAIN_HAND;
            case b:
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
                return EnumHand.a;
            case OFF_HAND:
                return EnumHand.b;
            default:
                throw new IllegalArgumentException("Unknown NMSHand: " + hand.name());
        }
    }

    public static EnumItemSlot toNMS(NMSItemSlot slot)
    {
        switch (slot)
        {
            case MAINHAND:
                return EnumItemSlot.a;
            case OFFHAND:
                return EnumItemSlot.b;
            case FEET:
                return EnumItemSlot.c;
            case LEGS:
                return EnumItemSlot.d;
            case CHEST:
                return EnumItemSlot.e;
            case HEAD:
                return EnumItemSlot.f;
            default:
                throw new IllegalArgumentException("Unknown NMSItemSlot: " + slot.name());
        }
    }

    public static NMSItemSlot fromNMS(EnumItemSlot slot)
    {
        switch (slot)
        {
            case a:
                return NMSItemSlot.MAINHAND;
            case b:
                return NMSItemSlot.OFFHAND;
            case c:
                return NMSItemSlot.FEET;
            case d:
                return NMSItemSlot.LEGS;
            case e:
                return NMSItemSlot.CHEST;
            case f:
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
                return EnumMoveType.a;
            case PLAYER:
                return EnumMoveType.b;
            case PISTON:
                return EnumMoveType.c;
            case SHULKER_BOX:
                return EnumMoveType.d;
            case SHULKER:
                return EnumMoveType.e;
            default:
                throw new IllegalArgumentException("Unknown NMSMoveType: " + moveType.name());
        }
    }

    public static NMSMoveType fromNMS(EnumMoveType moveType)
    {
        switch (moveType)
        {
            case a:
                return NMSMoveType.SELF;
            case b:
                return NMSMoveType.PLAYER;
            case c:
                return NMSMoveType.PISTON;
            case d:
                return NMSMoveType.SHULKER_BOX;
            case e:
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
                return EnumDirection.c;
            case SOUTH:
                return EnumDirection.d;
            case WEST:
                return EnumDirection.e;
            case EAST:
                return EnumDirection.f;
            case UP:
                return EnumDirection.b;
            case DOWN:
                return EnumDirection.a;
            default:
                throw new IllegalArgumentException("Unknown NMSDirection: " + direction.name());
        }
    }

    public static NMSDirection fromNMS(EnumDirection direction)
    {
        switch (direction)
        {
            case a:
                return NMSDirection.DOWN;
            case b:
                return NMSDirection.UP;
            case c:
                return NMSDirection.NORTH;
            case d:
                return NMSDirection.SOUTH;
            case e:
                return NMSDirection.WEST;
            case f:
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
