package net.kunmc.lab.scenamatica.commons.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@UtilityClass
public class MapUtils
{
    public static <K, V> void putIfNotNull(Map<K, V> map, K key, V value)
    {
        if (value != null)
            map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void putAsStrIfNotNull(Map<K, V> map, K key, V value)
    {
        if (value != null)
            map.put(key, (V) value.toString());
    }

    public static <K> void putPrimitiveOrStrIfNotNull(Map<K, Object> map, K key, Object value)
    {
        if (value != null)
            if (value instanceof Number || value instanceof Boolean || value instanceof Character)
                map.put(key, value);
            else
                map.put(key, value.toString());
    }

    public static <K> void putPrimitiveOrStrIfNotEmpty(Map<K, Object> map, K key, @Nullable Collection<?> value)
    {
        if (!(value == null || value.isEmpty()))
            for (Object o : value)
                if (o instanceof Number || o instanceof Boolean || o instanceof Character)
                    map.put(key, o);
                else
                    map.put(key, o.toString());
    }

    public static <K> void putListIfNotEmpty(Map<K, Object> map, K key, @Nullable Collection<?> value)
    {
        if (!(value == null || value.isEmpty()))
            map.put(key, value);
    }

    public static <K> void putMapIfNotEmpty(Map<K, Object> map, K key, @Nullable Map<?, ?> value)
    {
        if (!(value == null || value.isEmpty()))
            map.put(key, value);
    }

    public static <K> void putPrimitiveOrStrListIfNotEmpty(Map<K, Object> map, K key, @Nullable Collection<?> value)
    {
        if (!(value == null || value.isEmpty()))
        {
            List<Object> newList = new ArrayList<>();
            for (Object o : value)
                if (o instanceof Number || o instanceof Boolean || o instanceof Character)
                    newList.add(o);
                else
                    newList.add(o.toString());

            map.put(key, newList);
        }
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

    public static void checkTypeIfContains(Map<String, Object> map, String key, Class<?> type)
    {
        if (map.containsKey(key) && !type.isInstance(map.get(key)))
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: " + type.getSimpleName() + ")");
    }

    public static void checkMapType(Map<?, ?> map, Class<?> keyType, Class<?> valueType)
    {
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            if (!keyType.isInstance(entry.getKey()))
                throw new IllegalArgumentException("Unexpected type of key: " + entry.getKey() + " (expected: " + keyType.getSimpleName() + ")");
            if (!valueType.isInstance(entry.getValue()))
                throw new IllegalArgumentException("Unexpected type of value: " + entry.getValue() + " (expected: " + valueType.getSimpleName() + ")");
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> checkAndCastMap(Object mayMap, Class<K> keyType, Class<V> valueType)
    {
        if (!(mayMap instanceof Map))
            throw new IllegalArgumentException("Unexpected type of value: " + mayMap + " (expected: Map)");

        Map<?, ?> map = (Map<?, ?>) mayMap;
        checkMapType(map, keyType, valueType);

        return (Map<K, V>) map;
    }

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

    @NotNull
    public static <T extends Enum<T>> T getAsEnum(@NotNull Map<String, Object> map, @NotNull String key, @NotNull Class<T> enumType)
    {
        if (!map.containsKey(key))
            throw new IllegalArgumentException("Map does not contain key: " + key);
        return Enum.valueOf(enumType, (String) map.get(key));
    }

    @NotNull
    public static <T extends Enum<T>> List<T> getAsEnumOrEmptyList(@NotNull Map<String, Object> map, @NotNull String key, @NotNull Class<T> enumType)
    {
        if (!map.containsKey(key))
            return new ArrayList<>();
        List<T> list = new ArrayList<>();
        for (Object o : (List<?>) map.get(key))
            list.add(Enum.valueOf(enumType, (String) o));
        return list;
    }

    public static <T extends Enum<T>> void checkEnumNameIfContains(Map<String, Object> map, String key, Class<T> enumClass)
    {
        if (map.containsKey(key))
            MapUtils.checkEnumName(map, key, enumClass);
    }
}
