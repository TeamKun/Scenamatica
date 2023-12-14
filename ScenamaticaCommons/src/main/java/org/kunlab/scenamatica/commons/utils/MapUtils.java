package org.kunlab.scenamatica.commons.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@UtilityClass
public class MapUtils
{
    public static boolean equals(Map<?, ?> expected, Map<?, ?> actual)
    {
        if (expected == null || actual == null)
            return expected == actual;

        if (expected.size() != actual.size())
            return false;

        return isAdequate(expected, actual);
    }

    public static boolean equals(List<?> expected, List<?> actual)
    {
        if (expected == null || actual == null)
            return expected == actual;

        if (expected.size() != actual.size())
            return false;

        return isAdequate(expected, actual);
    }

    public static boolean isAdequate(Map<?, ?> required, Map<?, ?> target)
    {
        if (required == null || target == null)
            return required == target;

        for (Map.Entry<?, ?> entry : required.entrySet())
        {
            if (!target.containsKey(entry.getKey()))
                return false;

            if (entry.getValue() instanceof Map)
            {
                if (!isAdequate((Map<?, ?>) entry.getValue(), (Map<?, ?>) target.get(entry.getKey())))
                    return false;
                continue;
            }
            else if (entry.getValue() instanceof List)
            {
                if (!isAdequate((List<?>) entry.getValue(), (List<?>) target.get(entry.getKey())))
                    return false;
                continue;
            }

            if (!Objects.equals(entry.getValue(), target.get(entry.getKey())))
                return false;
        }

        return true;
    }

    public static boolean isAdequate(List<?> required, List<?> target)
    {
        if (required == null || target == null)
            return required == target;

        for (int i = 0; i < required.size(); i++)
        {
            if (required.get(i) instanceof Map)
            {
                if (!isAdequate((Map<?, ?>) required.get(i), (Map<?, ?>) target.get(i)))
                    return false;
                continue;
            }
            else if (required.get(i) instanceof List)
            {
                if (!isAdequate((List<?>) required.get(i), (List<?>) target.get(i)))
                    return false;
                continue;
            }

            Object requiredValue = required.get(i);
            Object targetValue = target.get(i);

            if (!Objects.equals(requiredValue, targetValue))
                return false;
        }

        return true;
    }

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

    public static void checkNumber(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        if (!(map.get(key) instanceof Number))
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Number)");
    }

    public static void checkNumberIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key) && !(map.get(key) instanceof Number))
            throw new IllegalArgumentException("Unexpected type of key: " + key + " (expected: Number)");
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

    @SuppressWarnings("unchecked")
    public static Map<String, Object> checkAndCastMap(Object mayMap)
    {
        if (!(mayMap instanceof Map))
            throw new IllegalArgumentException("Unexpected type of value: " + mayMap + " (expected: Map)");

        return (Map<String, Object>) mayMap;
    }

    public static <T extends Enum<T>> void checkEnumName(Map<String, Object> map, String key, Class<T> enumType)
    {
        MapUtils.checkContainsKey(map, key);
        try
        {
            Enum.valueOf(enumType, ((String) map.get(key)).toUpperCase(Locale.ROOT));
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

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAsList(@NotNull Map<String, Object> map, @NotNull String key)
    {
        MapUtils.checkContainsKey(map, key);
        MapUtils.checkType(map, key, List.class);
        return (List<T>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAsListOrEmpty(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return Collections.emptyList();
        MapUtils.checkType(map, key, List.class);
        return (List<T>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAsListOrDefault(@NotNull Map<String, Object> map, @NotNull String key, @Nullable List<T> defaultValue)
    {
        if (!map.containsKey(key))
            return defaultValue;
        MapUtils.checkType(map, key, List.class);
        return (List<T>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAsListOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        MapUtils.checkType(map, key, List.class);
        return (List<T>) map.get(key);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getOrNull(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return null;
        return (T) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getOrEmptyList(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return Collections.emptyList();

        return (List<T>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getOrEmptyMap(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return Collections.emptyMap();

        return (Map<K, V>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getAndCastOrEmptyMap(@NotNull Map<String, Object> map, @NotNull String key)
    {
        if (!map.containsKey(key))
            return Collections.emptyMap();

        return checkAndCastMap(map.get(key));
    }

    @Nullable
    public static <T extends Enum<T>> T getAsEnumOrNull(@NotNull Map<String, Object> map, @NotNull String key, @NotNull Class<T> enumType)
    {
        if (!map.containsKey(key))
            return null;
        return Enum.valueOf(enumType, ((String) map.get(key)).toUpperCase(Locale.ROOT));
    }

    @NotNull
    public static <T extends Enum<T>> T getAsEnum(@NotNull Map<String, Object> map, @NotNull String key, @NotNull Class<T> enumType)
    {
        if (!map.containsKey(key))
            throw new IllegalArgumentException("Map does not contain key: " + key);
        return Enum.valueOf(enumType, ((String) map.get(key)).toUpperCase(Locale.ROOT));
    }

    @NotNull
    public static <T extends Enum<T>> T getAsEnumOrDefault(@NotNull Map<String, Object> map, @NotNull String key, @NotNull Class<T> enumType, @NotNull T defaultValue)
    {
        if (!map.containsKey(key))
            return defaultValue;

        return Enum.valueOf(enumType, ((String) map.get(key)).toUpperCase(Locale.ROOT));
    }

    @NotNull
    public static <T extends Enum<T>> List<T> getAsEnumOrEmptyList(@NotNull Map<String, Object> map, @NotNull String key, @NotNull Class<T> enumType)
    {
        if (!map.containsKey(key))
            return new ArrayList<>();
        List<T> list = new ArrayList<>();
        for (Object o : (List<?>) map.get(key))
            list.add(Enum.valueOf(enumType, ((String) o).toUpperCase(Locale.ROOT)));
        return list;
    }

    public static <T extends Enum<T>> void checkEnumNameIfContains(Map<String, Object> map, String key, Class<T> enumClass)
    {
        if (map.containsKey(key))
            MapUtils.checkEnumName(map, key, enumClass);
    }

    public static void checkMaterialNameIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            MapUtils.checkMaterialName(map, key);
    }

    public static void checkMaterialName(Map<String, Object> map, String key)
    {
        MapUtils.checkContainsKey(map, key);
        Material mat = Utils.searchMaterial((String) map.get(key));
        if (mat == null)
            throw new IllegalArgumentException("Unexpected value of key: " + key + " (expected: Material-like), actual: " + map.get(key));
    }

    public static Optional<Number> getAsNumber(Map<String, Object> map, String key)
    {
        if (!map.containsKey(key))
            return Optional.empty();
        return Optional.of((Number) map.get(key));
    }

    public static Number getAsNumberOrNull(Map<String, Object> map, String key)
    {
        if (!map.containsKey(key))
            return null;
        return (Number) map.get(key);
    }

    public static <T extends Number> T getAsNumber(Map<String, Object> map, String key, Function<Number, T> converter)
    {
        if (!map.containsKey(key))
            throw new IllegalArgumentException("Map does not contain key: " + key);
        return converter.apply((Number) map.get(key));
    }

    public static <T extends Number> T getAsNumberOrNull(Map<String, Object> map, String key, Function<? super Number, T> converter)
    {
        if (!map.containsKey(key))
            return null;
        return converter.apply((Number) map.get(key));
    }

    public static Number getAsNumberSafe(Map<String, Object> map, String key)
    {
        if (!map.containsKey(key))
            return -1;
        return (Number) map.get(key);
    }

    public static Long getAsLongOrNull(Map<String, Object> map, String key)
    {
        if (!map.containsKey(key))
            return null;
        return ((Number) map.get(key)).longValue();
    }

    public static long getAsLongOrDefault(Map<String, Object> map, String key, long defaultValue)
    {
        if (!map.containsKey(key))
            return defaultValue;
        return ((Number) map.get(key)).longValue();
    }

    public static int getAsIntOrDefault(Map<String, Object> map, String key, int defaultValue)
    {
        if (!map.containsKey(key))
            return defaultValue;
        return ((Number) map.get(key)).intValue();
    }
}
