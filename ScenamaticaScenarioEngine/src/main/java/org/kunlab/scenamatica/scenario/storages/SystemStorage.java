package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;

public class SystemStorage extends AbstractVariableProvider implements ChildStorage
{
    public SystemStorage()
    {
        super(null);
    }

    @Override
    public String getKey()
    {
        return "system";
    }

    @Override
    public @NotNull Object get(@NotNull String key)
    {
        String prop = System.getProperty(key);
        if (prop == null)
            throw new BrokenReferenceException("System property '" + key + "' not found", key);
        else
            return prop;
    }

    @Override
    public void set(@NotNull String key, @Nullable Object value)
    {
        throw new UnsupportedOperationException();
    }
}
