package org.kunlab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Locale;

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

    public static Location assignWorldToLocation(@NotNull Location loc, @NotNull ScenarioEngine engine)
    {
        if (loc.getWorld() != null)
            return loc;

        Location newLoc = loc.clone();
        newLoc.setWorld(engine.getContext().getStage());

        return newLoc;
    }

    public static Material searchMaterial(@NotNull String name)
    {
        String nameLower = name.toLowerCase(Locale.ROOT);
        Material material;
        if ((material = Material.getMaterial(nameLower)) != null)
            return material;

        try
        {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ignored)
        {
        }

        if (name.contains(":"))
        {
            String[] split = name.split(":");  // minecraft:stone みたいなやつ
            return Material.getMaterial(split[0].toUpperCase(Locale.ROOT));
        }

        // Legacy は, サーバのオーバヘッドが走るので, できるだけ使いたくない。
        if ((material = Material.getMaterial(nameLower, /* legacy: */ true)) != null)
            return material;
        else
            return null;
    }
}
