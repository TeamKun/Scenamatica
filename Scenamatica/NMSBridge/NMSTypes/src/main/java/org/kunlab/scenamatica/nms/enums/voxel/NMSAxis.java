package org.kunlab.scenamatica.nms.enums.voxel;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 軸を表す列挙型です。
 */
public enum NMSAxis implements Predicate<NMSDirection>
{
    X("x")
            {
                public int get(int x, int y, int z)
                {
                    return x;
                }

                public double get(double x, double y, double z)
                {
                    return x;
                }
            },
    Y("y")
            {
                public int get(int x, int y, int z)
                {
                    return y;
                }

                public double get(double x, double y, double z)
                {
                    return y;
                }
            },
    Z("z")
            {
                public int get(int x, int y, int z)
                {
                    return z;
                }

                public double get(double x, double y, double z)
                {
                    return z;
                }
            };

    private static final NMSAxis[] VALUES = values();
    private static final Map<String, NMSAxis> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap((axis) -> axis.name, (axis) -> axis));
    private final String name;

    NMSAxis(String name)
    {
        this.name = name;
    }

    @Nullable
    public static NMSAxis byName(String name)
    {
        return BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean isVertical()
    {
        return this == Y;
    }

    public boolean isHorizontal()
    {
        return this == X || this == Z;
    }

    public String toString()
    {
        return this.name;
    }

    public NMSDirectionLimit getDirectionLimit()
    {
        switch (this)
        {
            case X:
            case Z:
                return NMSDirectionLimit.HORIZONTAL;
            case Y:
                return NMSDirectionLimit.VERTICAL;
            default:
                throw new Error("Someone's been tampering with the universe!");
        }
    }

    @Override
    public boolean test(@Nullable NMSDirection direction)
    {
        return direction != null && direction.getAxis() == this;
    }

    public abstract int get(int x, int y, int z);

    public abstract double get(double x, double y, double z);
}
