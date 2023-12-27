package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class AbstractVariableProvider
{
    public static String KEY_SEPARATOR = ".";

    private final Map<String, Object> map;

    public AbstractVariableProvider()
    {
        this.map = new ConcurrentHashMap<>();
    }

    public AbstractVariableProvider(@Nullable Map<String, ?> map)
    {
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
        return key.split(KEY_SEPARATOR);
    }

    protected static String[] sliceKey(String key, int startIdx)
    {
        String[] keys = splitKey(key);
        return sliceKey(keys, startIdx);
    }

    protected static String[] sliceKey(String[] keys, int startIdx)
    {
        if (keys.length <= startIdx)
            return new String[0];

        String[] result = new String[keys.length - startIdx];
        System.arraycopy(keys, startIdx, result, 0, result.length);
        return result;
    }

    protected static Object get(@NotNull Map<String, ?> map, @NotNull String key)
    {
        String[] keys = splitKey(key);
        return get(map, keys);
    }

    protected static Object get(@NotNull Map<String, ?> map, @NotNull String[] keys)
    {
        if (keys.length == 0)
            return null;

        for (int i = 0; i < keys.length - 1; i++)
        {
            Object value = map.get(keys[i]);
            if (value instanceof Map)
                // noinspection unchecked
                map = (Map<String, Object>) value;
            else if (value instanceof Function)
                return value;
            else
                throw new IllegalArgumentException("Unknown key '" + String.join(", ", keys) + "'");
        }

        return map.get(keys[keys.length - 1]);
    }

    protected void putAll(@NotNull Map<String, ?> map)
    {
        assert this.map != null;
        this.map.putAll(map);
    }

    public @Nullable Object get(@NotNull String key)
    {
        assert this.map != null;
        Object value = get(this.map, key);
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
        String[] keys = key.split(KEY_SEPARATOR);
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
}
