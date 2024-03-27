package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;

public class RuntimeStorage extends AbstractVariableProvider implements ChildStorage
{
    public static final String KEY_MEMORY = "memory";
    public static final String KEY_MEMORY_FREE = "free";
    public static final String KEY_MEMORY_TOTAL = "total";
    public static final String KEY_MEMORY_MAX = "max";

    public RuntimeStorage()
    {
        super(null);
    }

    private static Object processMemory(String[] keys)
    {
        if (keys.length == 0)
            throw new BrokenReferenceException("Empty memory key", null);

        String key = keys[0];
        if (key.equalsIgnoreCase(KEY_MEMORY_FREE))
            return Runtime.getRuntime().freeMemory();
        else if (key.equalsIgnoreCase(KEY_MEMORY_TOTAL))
            return Runtime.getRuntime().totalMemory();
        else if (key.equalsIgnoreCase(KEY_MEMORY_MAX))
            return Runtime.getRuntime().maxMemory();
        else
            throw new BrokenReferenceException(key);
    }

    @Override
    public @Nullable Object get(@NotNull String key)
    {
        String[] keys = splitKey(key);
        if (keys.length <= 2)
            throw new BrokenReferenceException(key);
        String ns = keys[0];
        String[] subKeys = sliceKey(key, 1);

        if (ns.equalsIgnoreCase(KEY_MEMORY))
            return processMemory(subKeys);
        else
            throw new BrokenReferenceException("Unknown namespace: " + ns, ns);
    }

    @Override
    public String getKey()
    {
        return "runtime";
    }
}
