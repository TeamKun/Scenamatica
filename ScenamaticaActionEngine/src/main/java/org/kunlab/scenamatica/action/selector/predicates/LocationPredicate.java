package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.action.selector.compiler.RangedNumber;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;

import java.util.Map;

public class LocationPredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_STRUCTURE_LOCATION = "location";
    public static final String KEY_STRUCTURE_LOCATION_SHORT = "loc";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_Z = "z";
    public static final String KEY_YAW = "yaw";
    public static final String KEY_PITCH = "pitch";
    public static final String KEY_WORLD = "world";

    @Override
    public boolean test(Entity entity, Map<? super String, Object> properties)
    {
        if (properties.containsKey(KEY_WORLD))
        {
            NamespacedKey worldNS = NamespaceUtils.fromString(properties.get(KEY_WORLD).toString());
            if (!NamespaceUtils.equalsIgnoreCase(entity.getWorld().getKey(), worldNS))
                return false;
        }

        if (!properties.containsKey(KEY_STRUCTURE_LOCATION))
            return true;

        Map<String, Object> locationDefs = MapUtils.checkAndCastMap(properties.get(KEY_STRUCTURE_LOCATION));

        return isInRangeOrUnspecified(KEY_X, locationDefs)
                && isInRangeOrUnspecified(KEY_Y, locationDefs)
                && isInRangeOrUnspecified(KEY_Z, locationDefs)
                && isInRangeOrUnspecified(KEY_YAW, locationDefs)
                && isInRangeOrUnspecified(KEY_PITCH, locationDefs);

    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        if (properties.containsKey(KEY_STRUCTURE_LOCATION_SHORT))  // 外に出す
        {
            properties.putAll(MapUtils.checkAndCastMap(properties.get(KEY_STRUCTURE_LOCATION_SHORT)));
            properties.remove(KEY_STRUCTURE_LOCATION_SHORT);
        }

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
