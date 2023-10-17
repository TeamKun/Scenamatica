package org.kunlab.scenamatica.action.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class LocationComparator
{
    // 比較時の誤差
    public static double EPSILON = 0.01;

    /**
     * Location を比較する。
     * 比較結果は l1 を原点とした３次元座標の距離を返す。
     * ワールドが違うなどの要因で比較できない場合は, null を返す。
     *
     * @param l1 原点となる Location
     * @param l2 比較対象となる Location
     * @return 比較結果
     */
    public static Double compare(@Nullable Location l1, @Nullable Location l2)
    {
        if (l1 == null || l2 == null)
            return null;

        Location l1Clone = l1.clone();
        Location l2Clone = l2.clone();

        World l1World = l1Clone.getWorld();
        World l2World = l2Clone.getWorld();
        if (!(l1World == null || l2World == null) && !l1World.equals(l2World))
            return null;

        return compare(l1Clone.toVector(), l2Clone.toVector());
    }

    /**
     * Vector を比較する。
     *
     * @param v1 原点となる Vector
     * @param v2 比較対象となる Vector
     * @return 比較結果
     */
    public static Double compare(@Nullable Vector v1, @Nullable Vector v2)
    {
        if (v1 == null || v2 == null)
            return null;

        double x = v2.getX() - v1.getX();
        double y = v2.getY() - v1.getY();
        double z = v2.getZ() - v1.getZ();

        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Location を比較し、同じ座標かどうかを返す。
     *
     * @param l1 原点となる Location
     * @param l2 比較対象となる Location
     * @return 同じ座標かどうか
     * @see LocationComparator#compare(Location, Location)
     * @see LocationComparator#EPSILON
     */
    public static boolean equals(@Nullable Location l1, @Nullable Location l2)
    {
        Double result = compare(l1, l2);

        if (result == null)
            return false;

        return Math.abs(result) < EPSILON;
    }

    /**
     * Vector と Location を比較し、同じ座標かどうかを返す。
     *
     * @param l 原点となる Location
     * @param v 比較対象となる Vector
     * @return 同じ座標かどうか
     */
    public static Double compare(@Nullable Location l, @Nullable Vector v)
    {
        if (l == null || v == null)
            return null;

        return compare(l.toVector(), v);
    }

    /**
     * Vector を比較し、同じ座標かどうかを返す。
     *
     * @param v1 原点となる Vector
     * @param v2 比較対象となる Vector
     * @return 同じ座標かどうか
     * @see LocationComparator#compare(Vector, Vector)
     * @see LocationComparator#EPSILON
     */
    public static boolean equals(@Nullable Vector v1, @Nullable Vector v2)
    {
        Double result = compare(v1, v2);

        if (result == null)
            return false;

        return Math.abs(result) < EPSILON;
    }
}
