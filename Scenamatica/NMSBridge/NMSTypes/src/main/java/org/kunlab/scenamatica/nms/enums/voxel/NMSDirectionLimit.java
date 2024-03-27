package org.kunlab.scenamatica.nms.enums.voxel;

import com.google.common.collect.Iterators;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 方向の制限を表します。
 */
@Getter
public enum NMSDirectionLimit implements Iterable<NMSDirection>, Predicate<NMSDirection>
{
    /**
     * 水平方向の制限です。
     */
    HORIZONTAL(new NMSDirection[]{NMSDirection.NORTH, NMSDirection.EAST, NMSDirection.SOUTH, NMSDirection.WEST}, new NMSAxis[]{NMSAxis.X, NMSAxis.Z}),
    /**
     * 垂直方向の制限です。
     */
    VERTICAL(new NMSDirection[]{NMSDirection.UP, NMSDirection.DOWN}, new NMSAxis[]{NMSAxis.Y});

    private final NMSDirection[] directions;
    private final NMSAxis[] axes;

    NMSDirectionLimit(NMSDirection[] directions, NMSAxis[] axes)
    {
        this.directions = directions;
        this.axes = axes;
    }

    public boolean test(@Nullable NMSDirection direction)
    {
        return direction != null && direction.getAxis().getDirectionLimit() == this;
    }

    public @NotNull Iterator<NMSDirection> iterator()
    {
        return Iterators.forArray(this.directions);
    }

    public Stream<NMSDirection> stream()
    {
        return Arrays.stream(this.directions);
    }
}
