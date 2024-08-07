package org.kunlab.scenamatica.commons.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.util.Locale;
import java.util.UUID;

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
        newLoc.setWorld(engine.getContext().getStage().getWorld());

        return newLoc;
    }

    public static Location assignWorldToBlockLocation(@NotNull BlockStructure block, @NotNull ScenarioEngine engine)
    {
        return assignWorldToLocation(block.getLocation(), engine);
    }

    public static Location assignWorldToLocation(@NotNull LocationStructure location, @NotNull ScenarioEngine engine)
    {
        return assignWorldToLocation(location.create(), engine);
    }

    public static boolean isEqualLocation(@NotNull Location loc1, @NotNull Location loc2)
    {
        return (loc1.getWorld() == null || loc2.getWorld() == null || loc1.getWorld().getUID().equals(loc2.getWorld().getUID()))
                && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    public static Material searchMaterial(@Nullable String name)
    {
        if (name == null)
            return null;

        String nameLower = name.toLowerCase(Locale.ROOT);
        Material material;
        if ((material = Material.getMaterial(nameLower)) != null)
            return material;
        else if ((material = Material.matchMaterial(nameLower)) != null)
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
        if ((material = Material.matchMaterial(nameLower, /* legacy: */ true)) != null)
            return material;
        else
            return null;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static EntityType searchEntityType(@Nullable String name)
    {
        if (name == null)
            return null;

        String searchName = name.toUpperCase(Locale.ROOT);
        if (name.contains(":")) // Namespace mode
        {
            String[] split = name.split(":");
            searchName = split[1];
        }

        try
        {
            int mayId = Integer.parseInt(searchName);
            return EntityType.fromId(mayId);
        }
        catch (NumberFormatException ignored)
        {
        }

        try
        {
            return EntityType.valueOf(searchName);
        }
        catch (IllegalArgumentException ignored)
        {
        }

        EntityType type = EntityType.fromName(searchName);
        if (type == null)
            throw new IllegalArgumentException("Unknown entity type: " + name);

        return type;
    }

    public static int[] convertUUIDToIntegers(UUID uuid)
    {
        int[] uuidIntegers = new int[4];
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        uuidIntegers[0] = (int) (mostSigBits >> 32);
        uuidIntegers[1] = (int) mostSigBits;
        uuidIntegers[2] = (int) (leastSigBits >> 32);
        uuidIntegers[3] = (int) leastSigBits;

        return uuidIntegers;
    }
}
