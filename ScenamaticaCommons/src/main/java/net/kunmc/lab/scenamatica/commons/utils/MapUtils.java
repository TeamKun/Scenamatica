package net.kunmc.lab.scenamatica.commons.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@UtilityClass
public class MapUtils
{
    public static <K, V> void putIfNotNull(Map<K, V> map, K key, V value)
    {
        if (value != null)
            map.put(key, value);
    }

    public static <K> void putPrimitiveOrStrIfNotNull(Map<K, Object> map, K key, Object value)
    {
        if (value != null)
            if (value instanceof Number || value instanceof Boolean || value instanceof Character)
                map.put(key, value);
            else
                map.put(key, value.toString());
    }

    public static void checkContainsKey(Map<String, Object> map, String... keys)
    {
        for (String key : keys)
            checkContainsKey(map, key);
    }

    public static void checkContainsKey(Map<String, Object> map, String key)
    {
        if (!map.containsKey(key))
            throw new IllegalArgumentException("Map does not contain key: " + key);
    }

    public static void checkType(Map<String, Object> map, String key, Class<?> type)
    {
        MapUtils.checkContainsKey(map, key);
        if (!type.isInstance(map.get(key)))
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: " + type.getSimpleName() + ")");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> void checkEnumName(Map<String, Object> map, String key, Class<T> enumType)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Enum.valueOf(enumType, (String) map.get(key));
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Unexpected value of key: " + key + " (expected: " + enumType.getSimpleName() + ")");
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(@NotNull Map<String, Object> map, @NotNull String key, @Nullable T defaultValue)
    {
        if (!map.containsKey(key))
            return defaultValue;
        return (T) map.get(key);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return (T) map.get(key);
    }

    @Nullable
    public static <T extends Enum<T>> T getAsEnumOrNull(@NotNull Map<String, Object> map, @NotNull String key, @NotNull Class<T> enumType)
    {
        if (!map.containsKey(key))
            return null;
        return Enum.valueOf(enumType, (String) map.get(key));
    }



    public static <T extends Enum<T>> void checkEnumNameIfContains(Map<String, Object> map, String key, Class<T> enumClass)
    {
        if (map.containsKey(key))
            MapUtils.checkEnumName(map, key, enumClass);
    }
}
