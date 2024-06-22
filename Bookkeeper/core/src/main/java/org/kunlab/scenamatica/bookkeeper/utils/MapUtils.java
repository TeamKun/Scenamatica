package org.kunlab.scenamatica.bookkeeper.utils;

import java.util.Map;

public class MapUtils
{
    public static String putIfNotNull(Map<? super String, Object> map, String key, Object value)
    {
        if (value != null)
        {
            map.put(key, value);
            return key;
        }
        return null;
    }

    public static boolean putIfTrue(Map<? super String, Object> map, String key, boolean value)
    {
        if (value)
        {
            map.put(key, true);
            return true;
        }
        return false;
    }

    public static <T> T[] putIfNotEmpty(Map<? super String, Object> map, String key, T[] value)
    {
        if (value != null && value.length > 0)
        {
            map.put(key, value);
            return value;
        }
        return null;
    }
}
