package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.selector.compiler.RangedNumber;

import java.util.Map;

public class DistancePredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_DISTANCE_2 = "r";
    public static final String KEY_DISTANCE_3 = "range";
    public static final String KEY_VOXEL_PREFIX = "d";
    public static final String KEY_VOXEL_X = "x";
    public static final String KEY_VOXEL_Y = "y";
    public static final String KEY_VOXEL_Z = "z";

    public static final String KEY_VOXEL_FULL_X = KEY_VOXEL_PREFIX + KEY_VOXEL_X;
    public static final String KEY_VOXEL_FULL_Y = KEY_VOXEL_PREFIX + KEY_VOXEL_Y;
    public static final String KEY_VOXEL_FULL_Z = KEY_VOXEL_PREFIX + KEY_VOXEL_Z;

    private static void normalizeDistanceMap(Map<? super String, Object> distanceStruct)
    {
        integrateAlias(distanceStruct, KEY_VOXEL_X, KEY_VOXEL_FULL_X);
        integrateAlias(distanceStruct, KEY_VOXEL_Y, KEY_VOXEL_FULL_Y);
        integrateAlias(distanceStruct, KEY_VOXEL_Z, KEY_VOXEL_FULL_Z);

        RangedNumber.normalizeMap(KEY_VOXEL_FULL_X, distanceStruct);
        RangedNumber.normalizeMap(KEY_VOXEL_FULL_Y, distanceStruct);
        RangedNumber.normalizeMap(KEY_VOXEL_FULL_Z, distanceStruct);
    }

    private static boolean processRangedDistance(Player basis, Entity entity, RangedNumber distance)
    {
        double distanceSquared = basis.getLocation().distanceSquared(entity.getLocation());
        return distance.test(distanceSquared);
    }

    private static boolean processMapDistance(Player basis, Entity entity, Map<String, Object> properties)
    {
        double dx = -1, dy = -1, dz = -1;
        if (properties.containsKey(KEY_VOXEL_X))
            dx = Math.abs(basis.getLocation().getX() - entity.getLocation().getX());
        if (properties.containsKey(KEY_VOXEL_Y))
            dy = Math.abs(basis.getLocation().getY() - entity.getLocation().getY());
        if (properties.containsKey(KEY_VOXEL_Z))
            dz = Math.abs(basis.getLocation().getZ() - entity.getLocation().getZ());

        return (dx == -1 || ((RangedNumber) properties.get(KEY_VOXEL_X)).test(dx))
                && (dy == -1 || ((RangedNumber) properties.get(KEY_VOXEL_Y)).test(dy))
                && (dz == -1 || ((RangedNumber) properties.get(KEY_VOXEL_Z)).test(dz));
    }

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        Object distance = properties.get(KEY_DISTANCE);
        if (distance instanceof Map)
            return processMapDistance(basis, entity, MapUtils.checkAndCastMap(distance));
        else if (distance instanceof RangedNumber)
            return processRangedDistance(basis, entity, (RangedNumber) distance);

        throw new IllegalStateException("DistancePredicate: not a map or ranged number");
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        expandItemsIfExists(KEY_DISTANCE_2, KEY_DISTANCE, properties);
        expandItemsIfExists(KEY_DISTANCE_3, KEY_DISTANCE, properties);


        RangedNumber.normalizeMap(KEY_DISTANCE, KEY_VOXEL_FULL_X, properties);
        RangedNumber.normalizeMap(KEY_DISTANCE, KEY_VOXEL_FULL_Y, properties);
        RangedNumber.normalizeMap(KEY_DISTANCE, KEY_VOXEL_FULL_Z, properties);

        Object distance = properties.get(KEY_DISTANCE);
        if (distance == null)
            return;

        if (distance instanceof Map)
            normalizeDistanceMap(MapUtils.checkAndCastMap(distance));
        else
            RangedNumber.normalizeMap(KEY_DISTANCE, KEY_DISTANCE, properties);
    }

    @Override
    public boolean isBasisRequired()
    {
        return true;
    }

    @Override
    public String[] getUsingRangedKeys()
    {
        return new String[]{
                KEY_DISTANCE,
                KEY_DISTANCE_2,
                KEY_DISTANCE_3,
                KEY_VOXEL_PREFIX + KEY_VOXEL_X,
                KEY_VOXEL_PREFIX + KEY_VOXEL_Y,
                KEY_VOXEL_PREFIX + KEY_VOXEL_Z
        };
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_DISTANCE,
                KEY_DISTANCE_2,
                KEY_DISTANCE_3,
                KEY_VOXEL_PREFIX + KEY_VOXEL_X,
                KEY_VOXEL_PREFIX + KEY_VOXEL_Y,
                KEY_VOXEL_PREFIX + KEY_VOXEL_Z
        };
    }
}
