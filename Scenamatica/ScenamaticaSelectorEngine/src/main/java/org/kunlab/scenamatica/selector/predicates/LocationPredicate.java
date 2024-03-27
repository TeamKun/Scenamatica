package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.selector.compiler.NegateSupport;
import org.kunlab.scenamatica.selector.compiler.RangedNumber;

import java.util.Map;

public class LocationPredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_STRUCTURE_LOCATION = "location";
    public static final String KEY_STRUCTURE_LOCATION_SHORT = "loc";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_Z = "z";
    public static final String KEY_YAW = "yaw";
    public static final String KEY_YAW_2 = "y_rotation";
    public static final String KEY_YAW_3 = "ry";
    public static final String KEY_PITCH = "pitch";
    public static final String KEY_PITCH_2 = "x_rotation";
    public static final String KEY_PITCH_3 = "rx";
    public static final String KEY_WORLD = "world";

    protected static boolean isInRangeOrUnspecified(Number value, String key, Map<String, Object> properties)
    {
        if (!properties.containsKey(key))
            return true;

        return ((RangedNumber) properties.get(key)).test(value);
    }

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        if (properties.containsKey(KEY_WORLD))
        {
            String worldName = NegateSupport.getRawCast(KEY_WORLD, properties);

            boolean isInWorld = entity.getWorld().getName().equals(worldName);
            boolean doNegate = NegateSupport.shouldNegate(KEY_WORLD, properties);
            if (isInWorld == doNegate)
                return false;
        }

        if (!properties.containsKey(KEY_STRUCTURE_LOCATION))
            return true;

        Location location = entity.getLocation();
        Map<String, Object> locationDefs = MapUtils.checkAndCastMap(properties.get(KEY_STRUCTURE_LOCATION));

        return isInRangeOrUnspecified(location.getX(), KEY_X, locationDefs)
                && isInRangeOrUnspecified(location.getY(), KEY_Y, locationDefs)
                && isInRangeOrUnspecified(location.getZ(), KEY_Z, locationDefs)
                && isInRangeOrUnspecified(location.getYaw(), KEY_YAW, locationDefs)
                && isInRangeOrUnspecified(location.getPitch(), KEY_PITCH, locationDefs);
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        expandItemsIfExists(KEY_STRUCTURE_LOCATION_SHORT, KEY_STRUCTURE_LOCATION, properties);

        integrateAlias(properties, KEY_YAW, KEY_YAW_2);
        integrateAlias(properties, KEY_YAW, KEY_YAW_3);

        integrateAlias(properties, KEY_PITCH, KEY_PITCH_2);
        integrateAlias(properties, KEY_PITCH, KEY_PITCH_3);

        RangedNumber.normalizeMap(KEY_STRUCTURE_LOCATION, KEY_X, properties);
        RangedNumber.normalizeMap(KEY_STRUCTURE_LOCATION, KEY_Y, properties);
        RangedNumber.normalizeMap(KEY_STRUCTURE_LOCATION, KEY_Z, properties);
        RangedNumber.normalizeMap(KEY_STRUCTURE_LOCATION, KEY_YAW, properties);
        RangedNumber.normalizeMap(KEY_STRUCTURE_LOCATION, KEY_PITCH, properties);
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_STRUCTURE_LOCATION,
                KEY_STRUCTURE_LOCATION_SHORT,
                KEY_WORLD
        };
    }

    @Override
    public String[] getUsingRangedKeys()
    {
        return new String[]{
                KEY_X,
                KEY_Y,
                KEY_Z,
                KEY_PITCH,
                KEY_YAW
        };
    }
}
