package org.kunlab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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
}
