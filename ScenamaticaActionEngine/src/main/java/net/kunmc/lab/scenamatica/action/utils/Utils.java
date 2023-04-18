package net.kunmc.lab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.util.Vector;

@UtilityClass
public class Utils
{
    public static boolean vectorEquals(Vector v1, Vector v2, double epsilon)
    {
        return Math.abs(v1.getX() - v2.getX()) < epsilon &&
                Math.abs(v1.getY() - v2.getY()) < epsilon &&
                Math.abs(v1.getZ() - v2.getZ()) < epsilon;
    }

    public static boolean vectorEquals(Vector v1, Vector v2)
    {
        return vectorEquals(v1, v2, 0.0001);
    }
}
