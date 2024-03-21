package org.kunlab.scenamatica.nms.enums.voxel;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.nms.NMSElement;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 方向を表す列挙型です。
 */
@Getter
public enum NMSDirection implements NMSElement
{
    DOWN(1, -1, "down", NMSAxisDirection.NEGATIVE, NMSAxis.Y, new Location(null, 0, -1, 0)),
    UP(0, -1, "up", NMSAxisDirection.POSITIVE, NMSAxis.Y, new Location(null, 0, 1, 0)),
    NORTH(3, 2, "north", NMSAxisDirection.NEGATIVE, NMSAxis.Z, new Location(null, 0, 0, -1)),
    SOUTH(2, 0, "south", NMSAxisDirection.POSITIVE, NMSAxis.Z, new Location(null, 0, 0, 1)),
    WEST(5, 1, "west", NMSAxisDirection.NEGATIVE, NMSAxis.X, new Location(null, -1, 0, 0)),
    EAST(4, 3, "east", NMSAxisDirection.POSITIVE, NMSAxis.X, new Location(null, 1, 0, 0));

    private static final NMSDirection[] VALUES = values();
    private static final Map<String, NMSDirection> BY_NAME = Arrays.stream(VALUES)
            .collect(Collectors.toMap(NMSDirection::getName, direction -> direction));
    private static final NMSDirection[] BY_INDEX = Arrays.stream(VALUES)
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toArray(NMSDirection[]::new);
    private final int oppositeIndex;
    private final int rotationValue;
    private final String name;
    private final NMSAxis axis;
    private final NMSAxisDirection axisDirection;
    private final Location adjacentCoordinates;

    NMSDirection(int oppositeIndex, int rotationValue, String name, NMSAxisDirection axisDirection, NMSAxis axis, Location adjacentCoordinates)
    {
        this.rotationValue = rotationValue;
        this.oppositeIndex = oppositeIndex;
        this.name = name;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.adjacentCoordinates = adjacentCoordinates;
    }

    /**
     * エンティティの向きから方向を取得します。
     *
     * @param entity エンティティ
     * @return 方向
     */
    public static NMSDirection[] fromEntity(Entity entity)
    {
        float yawRadian = entity.getLocation().getYaw();
        float pitchRadian = -entity.getLocation().getPitch();

        float sinYaw = (float) Math.sin(yawRadian);
        float sinPitch = (float) Math.sin(pitchRadian);
        float cosPitch = (float) Math.cos(pitchRadian);

        boolean isPitchPositive = sinPitch > 0.0F;
        boolean isYawNegative = sinYaw < 0.0F;
        boolean isPitchNegative = cosPitch > 0.0F;

        float xzPitchAbs = isPitchPositive ? sinPitch: -sinPitch;
        float xzYawAbs = isYawNegative ? -sinYaw: sinYaw;
        float xzPitchNegAbs = isPitchNegative ? cosPitch: -cosPitch;
        float xzYawXformed = xzYawAbs * cosPitch;
        float xzPitchXformed = xzPitchNegAbs * cosPitch;

        NMSDirection xAxis = isPitchPositive ? EAST: WEST;
        NMSDirection yAxis = isYawNegative ? UP: DOWN;
        NMSDirection zAxis = isPitchNegative ? SOUTH: NORTH;

        if (xzPitchAbs > xzPitchXformed)
        {
            if (xzYawXformed > xzPitchAbs)
                return getDirections(yAxis, xAxis, zAxis);
            else
                return xzPitchXformed > xzYawXformed ? getDirections(xAxis, zAxis, yAxis): getDirections(xAxis, yAxis, zAxis);
        }
        else if (xzYawXformed > xzPitchXformed)
            return getDirections(yAxis, zAxis, xAxis);
        else
            return xzPitchAbs > xzYawXformed ? getDirections(zAxis, xAxis, yAxis): getDirections(zAxis, yAxis, xAxis);
    }

    /**
     * 方向を取得します。
     *
     * @param firstDirection  1番目の方向
     * @param secondDirection 2番目の方向
     * @param thirdDirection  3番目の方向
     * @return 方向
     */
    private static NMSDirection[] getDirections(NMSDirection firstDirection, NMSDirection secondDirection, NMSDirection thirdDirection)
    {
        return new NMSDirection[]{firstDirection, secondDirection, thirdDirection, thirdDirection.opposite(), secondDirection.opposite(), firstDirection.opposite()};
    }

    /**
     * 軸の方向から方向を取得します。
     *
     * @param axis          軸
     * @param axisDirection 軸の向き
     * @return 方向
     */
    public static NMSDirection fromAxisDirection(NMSAxis axis, NMSAxisDirection axisDirection)
    {
        switch (axis)
        {
            case X:
                return axisDirection == NMSAxisDirection.POSITIVE ? EAST: WEST;
            case Y:
                return axisDirection == NMSAxisDirection.POSITIVE ? UP: DOWN;
            case Z:
            default:
                return axisDirection == NMSAxisDirection.POSITIVE ? SOUTH: NORTH;
        }
    }

    /**
     * ベクトルから方向を取得します。
     *
     * @param x Xベクトル
     * @param y Yベクトル
     * @param z Zベクトル
     * @return 方向
     */
    public static NMSDirection from3DVector(double x, double y, double z)
    {
        return from3DVector((float) x, (float) y, (float) z);
    }

    /**
     * ベクトルから方向を取得します。
     *
     * @param x Xベクトル
     * @param y Yベクトル
     * @param z Zベクトル
     * @return 方向
     */
    public static NMSDirection from3DVector(float x, float y, float z)
    {
        NMSDirection closestDirection = NORTH;
        float closestDotProduct = Float.MIN_VALUE;
        for (NMSDirection direction : VALUES)
        {
            float dotProduct = x * (float) direction.adjacentCoordinates.getX() + y * (float) direction.adjacentCoordinates.getY() + z * (float) direction.adjacentCoordinates.getZ();
            if (dotProduct > closestDotProduct)
            {
                closestDotProduct = dotProduct;
                closestDirection = direction;
            }
        }
        return closestDirection;
    }

    /**
     * 軸の方向から方向を取得します。
     *
     * @param axisDirection 軸の向き
     * @param axis          軸
     * @return 方向
     */
    public static NMSDirection fromAxisDirection(NMSAxisDirection axisDirection, NMSAxis axis)
    {
        for (NMSDirection direction : VALUES)
            if (direction.getAxisDirection() == axisDirection && direction.getAxis() == axis)
                return direction;

        throw new IllegalArgumentException("No such direction: " + axisDirection + " " + axis);
    }

    public static NMSDirection fromBlockFace(BlockFace face)
    {
        switch (face)
        {
            case UP:
                return UP;
            case DOWN:
                return DOWN;
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            case EAST:
                return EAST;
            default:
                throw new IllegalArgumentException("Unknown BlockFace: " + face.name());
        }
    }

    /**
     * 対角線の方向を取得します。
     *
     * @return 対角線の方向
     */
    public NMSDirection opposite()
    {
        return BY_INDEX[this.oppositeIndex];
    }

    /**
     * 時計回りに回転した方向を取得します。
     *
     * @return 時計回りに回転した方向
     */
    public NMSDirection rotateClockwise()
    {
        switch (this)
        {
            case NORTH:
                return EAST;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            case EAST:
                return SOUTH;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        }
    }

    /**
     * 反時計回りに回転した方向を取得します。
     *
     * @return 反時計回りに回転した方向
     */
    public NMSDirection rotateCounterclockwise()
    {
        switch (this)
        {
            case NORTH:
                return WEST;
            case SOUTH:
                return EAST;
            case WEST:
                return SOUTH;
            case EAST:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    /**
     * 隣接するX座標を取得します。
     *
     * @return 隣接するX座標
     */
    public double getAdjacentX()
    {
        return this.adjacentCoordinates.getX();
    }

    /**
     * 隣接するY座標を取得します。
     *
     * @return 隣接するY座標
     */
    public double getAdjacentY()
    {
        return this.adjacentCoordinates.getY();
    }

    /**
     * 隣接するZ座標を取得します。
     *
     * @return 隣接するZ座標
     */
    public double getAdjacentZ()
    {
        return this.adjacentCoordinates.getZ();
    }

    /**
     * 回転角度を取得します。
     *
     * @return 回転角度
     */
    public float getRotationAngle()
    {
        return (float) ((this.rotationValue & 3) * 90);
    }

    public String toString()
    {
        return this.name;
    }

    /**
     * 方向を向いているかどうかを取得します。
     *
     * @param angle 角度
     * @return 方向を向いているかどうか
     */
    public boolean isFacingAngle(float angle)
    {
        float radians = angle * 0.017453292F;
        float sinAngle = (float) -Math.sin(radians);
        float cosAngle = (float) Math.cos(radians);

        return (float) this.adjacentCoordinates.getX() * sinAngle + (float) this.adjacentCoordinates.getZ() * cosAngle > 0.0F;
    }

    /**
     * エンティティが方向を向いているかどうかを取得します。
     *
     * @param entity エンティティ
     * @return 方向を向いているかどうか
     */
    public boolean isFacingAngle(Entity entity)
    {
        return this.isFacingAngle(entity.getLocation().getYaw());
    }
}
