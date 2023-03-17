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
    public static void checkEnumName(Map<String, Object> map, String key, Class<? extends Enum<?>> enumType)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Enum.valueOf((Class<Enum>) enumType, (String) map.get(key));
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

    @Nullable
    public static Long getAsLongOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return Long.parseLong((String) map.get(key));
    }

    @Nullable
    public static Integer getAsIntegerOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return Integer.parseInt((String) map.get(key));
    }

    @Nullable
    public static Boolean getAsBooleanOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return Boolean.parseBoolean((String) map.get(key));
    }

    @Nullable
    public static Double getAsDoubleOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return Double.parseDouble((String) map.get(key));
    }

    @Nullable
    public static Float getAsFloatOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return Float.parseFloat((String) map.get(key));
    }

    @Nullable
    public static Byte getAsByteOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return Byte.parseByte((String) map.get(key));
    }

    @Nullable
    public static Short getAsShortOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return Short.parseShort((String) map.get(key));
    }

    @Nullable
    public static Character getAsCharacterOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return ((String) map.get(key)).charAt(0);
    }

    public static void checkLongType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Long.parseLong((String) map.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Long)");
        }
    }

    public static void checkIntegerType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Integer.parseInt((String) map.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Integer)");
        }
    }

    public static void checkBooleanType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Boolean.parseBoolean((String) map.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Boolean)");
        }
    }

    public static void checkDoubleType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Double.parseDouble((String) map.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Double)");
        }
    }

    public static void checkFloatType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Float.parseFloat((String) map.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Float)");
        }
    }

    public static void checkByteType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Byte.parseByte((String) map.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Byte)");
        }
    }

    public static void checkShortType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Short.parseShort((String) map.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Short)");
        }
    }

    public static void checkContainsAndLongType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkLongType(map, key);
    }

    public static void checkContainsAndIntegerType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkIntegerType(map, key);
    }

    public static void checkContainsAndBooleanType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkBooleanType(map, key);
    }

    public static void checkContainsAndDoubleType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkDoubleType(map, key);
    }

    public static void checkContainsAndFloatType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkFloatType(map, key);
    }

    public static void checkContainsAndByteType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkByteType(map, key);
    }

    public static void checkContainsAndShortType(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkShortType(map, key);
    }

    public static void checkLongTypeIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkLongType(map, key);
    }

    public static void checkIntegerTypeIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkIntegerType(map, key);
    }

    public static void checkBooleanTypeIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkBooleanType(map, key);
    }

    public static void checkDoubleTypeIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkDoubleType(map, key);
    }

    public static void checkFloatTypeIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkFloatType(map, key);
    }

    public static void checkByteTypeIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkByteType(map, key);
    }

    public static void checkShortTypeIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkShortType(map, key);
    }

    public static void checkEnumNameIfContains(Map<String, Object> map, String key, Class<? extends Enum<?>> enumClass)
    {
        if (map.containsKey(key))
            MapUtils.checkEnumName(map, key, enumClass);
    }
}
