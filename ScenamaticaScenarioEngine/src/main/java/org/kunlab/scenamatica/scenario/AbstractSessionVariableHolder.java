package org.kunlab.scenamatica.scenario;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.SessionVariableHolder;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSessionVariableHolder implements SessionVariableHolder
{
    public static String KEY_SEPARATOR = ".";

    private final HashMap<String, Object> map;

    public AbstractSessionVariableHolder()
    {
        this.map = new HashMap<>();
    }

    @Override
    public @Nullable Object get(@NotNull String key)
    {
        String[] keys = key.split(KEY_SEPARATOR);
        if (keys.length == 0)
            return null;

        Map<String, Object> map = this.map;
        for (int i = 0; i < keys.length - 1; i++)
        {
            Object value = map.get(keys[i]);
            if (value instanceof Map)
                // noinspection unchecked
                map = (Map<String, Object>) value;
            else
                return null;
        }

        return map.get(keys[keys.length - 1]);
    }

    @Override
    public void set(@NotNull String key, @Nullable Object value)
    {
        String[] keys = key.split(KEY_SEPARATOR);
        if (keys.length == 0)
            return;

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
