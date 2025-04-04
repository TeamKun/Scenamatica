package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class AbstractVariableProvider
{
    public static String KEY_SEPARATOR = ".";

    protected final Map<String, Object> map;
    protected final StructureSerializer ser;

    public AbstractVariableProvider(@Nullable StructureSerializer ser)
    {
        this.map = new ConcurrentHashMap<>();
        this.ser = ser;
    }

    public AbstractVariableProvider()
    {
        this.map = new ConcurrentHashMap<>();
        this.ser = null;
    }

    public AbstractVariableProvider(@Nullable Map<String, ?> map, @Nullable StructureSerializer ser)
    {
        this.ser = ser;
        if (map == null)
            this.map = null;
        else
            this.map = new ConcurrentHashMap<>(map);
    }

    protected static Map<String, Function<String[], ?>> childMap(Object... kv)
    {
        if (kv.length % 2 != 0)
            throw new IllegalArgumentException("kv.length % 2 != 0");

        Map<String, Function<String[], ?>> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2)
            // noinspection unchecked
            map.put(kv[i].toString(), (Function<String[], ?>) kv[i + 1]);

        return map;
    }

    protected static Function<String[], ?> func(Function<String[], ?> supplier)
    {
        return supplier;
    }

    protected static String[] splitKey(String key)
    {
        return key.split(KEY_SEPARATOR.replace(".", "\\."));
    }

    protected static String[] sliceKey(String key, int startIdx)
    {
        String[] keys = splitKey(key);
        return sliceKey(keys, startIdx);
    }

    protected static String[] sliceKey(String[] keys, int startIdx)
    {
        return sliceKey(keys, startIdx, keys.length);
    }

    protected static String[] sliceKeyLast(String key, int endIdx)
    {
        String[] keys = splitKey(key);
        return sliceKey(keys, 0, endIdx);
    }

    protected static String[] sliceKeyLast(String[] keys, int endIdx)
    {
        return sliceKey(keys, 0, endIdx);
    }

    protected static String[] sliceKey(String[] keys, int startIdx, int endIdx)
    {
        if (keys.length <= startIdx)
            return new String[0];

        String[] result = new String[endIdx - startIdx];
        System.arraycopy(keys, startIdx, result, 0, result.length);
        return result;
    }

    protected static Object get(@NotNull Map<String, ?> map, @NotNull String key)
    {
        return get(map, key, null);
    }

    protected static Object get(@NotNull Map<String, ?> map, @NotNull String[] keys)
    {
        return get(map, keys, null);
    }

    protected static Object get(@NotNull Map<String, ?> map, @NotNull String key, @Nullable StructureSerializer ser)
    {
        String[] keys = splitKey(key);
        return get(map, keys, ser);
    }

    @SuppressWarnings("unchecked")
    protected static Object get(@NotNull Map<String, ?> map, @NotNull String[] keys, @Nullable StructureSerializer ser)
    {
        if (keys.length == 0)
            return null;

        int lastIndex = keys.length - 1;

        for (int i = 0; i < lastIndex; i++)
        {
            Object value = map.get(keys[i]);
            map = castToMapLikeOrThrow(value, keys, i, ser);
        }

        String key = keys[lastIndex];
        if (!map.containsKey(key))
        {
            String[] partiallyResolvedReference = new String[lastIndex];
            System.arraycopy(keys, 0, partiallyResolvedReference, 0, lastIndex);
            String partiallyResolvedReferenceStr = String.join(".", partiallyResolvedReference);

            throw new BrokenReferenceException(String.join(".", keys), partiallyResolvedReferenceStr, map);
        }

        Object value = map.get(keys[lastIndex]);
        if (value instanceof Function)
            return ((Function<String[], ?>) value).apply(new String[0]);
        else if (value instanceof Structure)
            return requireSerializer(ser).serialize((Structure) value, null);
        else if (shouldConvertToStructure(ser, value))
            return requireSerializer(ser).toStructure(value, null);
        else
            return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castToMapLikeOrThrow(@Nullable Object value, @NotNull String[] keys, int idx, @Nullable StructureSerializer ser)
    {
        if (value instanceof Map)
            return (Map<String, Object>) value;
        else if (value instanceof Structure)
            return requireSerializer(ser).serialize((Structure) value, null);
        else if (value instanceof Function)
            return ((Function<String[], Map<String, Object>>) value).apply(sliceKey(keys, idx + 1));
        else if (shouldConvertToStructure(ser, value))
        {
            Structure structure = requireSerializer(ser).toStructure(value, null);
            return requireSerializer(ser).serialize(structure, null);
        }
        else
            throw new BrokenReferenceException(String.join(".", keys), "Expected a map-like object, but got " + value);
    }

    private static boolean shouldConvertToStructure(@Nullable StructureSerializer ser, @Nullable Object value)
    {
        if (value == null || value.getClass().isPrimitive())
            return false;
        else
            return requireSerializer(ser).canConvertToStructure(value);
    }

    private static StructureSerializer requireSerializer(@Nullable StructureSerializer ser)
    {
        if (ser == null)
            throw new IllegalStateException("StructureSerializer is null");
        else
            return ser;
    }

    protected void putAll(@NotNull Map<String, ?> map)
    {
        assert this.map != null;
        this.map.putAll(map);
    }

    public @Nullable Object get(@NotNull String key)
    {
        assert this.map != null;
        Object value = get(this.map, key, this.ser);
        if (value instanceof Function)
        {
            String[] keys = sliceKey(key, 1);
            // noinspection rawtypes,unchecked
            return ((Function) value).apply(keys);
        }
        else
            return value;
    }

    public void set(@NotNull String key, @Nullable Object value)
    {
        String[] keys = key.split(KEY_SEPARATOR.replace(".", "\\."));
        if (keys.length == 0)
            return;

        assert this.map != null;

        Map<String, Object> map = this.map;
        for (int i = 0; i < keys.length - 1; i++)
        {
            Object v = map.get(keys[i]);
            if (v instanceof Map)
                // noinspection unchecked
                map = (Map<String, Object>) v;
            else
            {
                HashMap<String, Object> newMap = new HashMap<>();
                map.put(keys[i], newMap);
                map = newMap;
            }
        }

        map.put(keys[keys.length - 1], value);
    }

    public void clear()
    {
        if (this.map == null)
            return;
        this.map.clear();
    }
}
