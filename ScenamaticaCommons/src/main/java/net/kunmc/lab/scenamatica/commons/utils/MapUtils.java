package net.kunmc.lab.scenamatica.commons.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    public static Map<String, Object> locationToMap(Location loc)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());

        if (!(loc.getWorld() == null || loc.getWorld().getName().equals("world")))
            map.put("world", loc.getWorld().getName());

        if (loc.getYaw() != 0)
            map.put("yaw", loc.getYaw());
        if (loc.getPitch() != 0)
            map.put("pitch", loc.getPitch());

        return map;
    }

    public static Location fromMap(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, "x", "y", "z");
        MapUtils.checkType(map, "x", Number.class);
        MapUtils.checkType(map, "y", Number.class);
        MapUtils.checkType(map, "z", Number.class);

        World world = null;
        if (map.containsKey("world"))
        {
            MapUtils.checkType(map, "world", String.class);
            world = Bukkit.getWorld((String) map.get("world"));
            if (world == null)
                throw new IllegalArgumentException("World not found: " + map.get("world"));
        }

        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        float yaw = MapUtils.getOrDefault(map, "yaw", 0f);
        float pitch = MapUtils.getOrDefault(map, "pitch", 0f);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void checkLocation(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, "x", "y", "z");
        MapUtils.checkType(map, "x", Number.class);
        MapUtils.checkType(map, "y", Number.class);
        MapUtils.checkType(map, "z", Number.class);
    }

    public static void checkLocationIfContains(Map<String, Object> map, String key)
    {
        if (map.containsKey(key))
            checkLocation(MapUtils.checkAndCastMap(map.get(key), String.class, Object.class));
    }

    public static Location getAsLocation(Map<String, Object> map, String key)
    {
        return fromMap(MapUtils.checkAndCastMap(map.get(key), String.class, Object.class));
    }

    public static Location getAsLocationOrNull(Map<String, Object> map, String key)
    {
        if (!map.containsKey(key))
            return null;
        return fromMap(MapUtils.checkAndCastMap(map.get(key), String.class, Object.class));
    }

    public static void putLocationIfNotNull(Map<? super String, Object> map, String key, Location loc)
    {
        if (loc != null)
            map.put(key, locationToMap(loc));
    }

    public static Number getAsNumberOrNull(Map<String, Object> map, String key)
    {
        if (!map.containsKey(key))
            return null;
        return (Number) map.get(key);
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
