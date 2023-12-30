package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionStorageImpl extends AbstractVariableProvider implements SessionStorage
{
    private static final List<? extends ChildStorage> UNIQUE_STORAGES;

    static
    {
        List<ChildStorage> uniqueStorages = new ArrayList<>();

        // <editor-fold defaultstate="collapsed" desc="Initialize uniqueStorages">

        uniqueStorages.add(new SystemStorage());
        uniqueStorages.add(new RuntimeStorage());

        // </editor-fold>

        UNIQUE_STORAGES = Collections.unmodifiableList(uniqueStorages);
    }

    private final List<? extends ChildStorage> storages;

    public SessionStorageImpl(ScenamaticaRegistry registry, ScenarioSession session)
    {
        this.storages = this.createStorages(registry, session);
    }

    private List<? extends ChildStorage> createStorages(ScenamaticaRegistry registry, ScenarioSession session)
    {
        List<ChildStorage> storages = new ArrayList<>(UNIQUE_STORAGES);

        // <editor-fold defaultstate="collapsed" desc="Initialize storages">
        ScenarioStorage scenarioStorage = new ScenarioStorage(session, registry.getScenarioFileManager().getSerializer());
        storages.add(new org.kunlab.scenamatica.scenario.storages.SessionStorage(session, scenarioStorage));
        storages.add(scenarioStorage);
        // </editor-fold>

        return Collections.unmodifiableList(storages);
    }

    @Override
    public @Nullable Object get(@NotNull String key)
    {
        String[] keys = splitKey(key);
        if (keys.length <= 2)
            return null;
        String ns = keys[0];

        for (ChildStorage storage : this.storages)
        {
            if (storage.getKey().equalsIgnoreCase(ns))
                return storage.get(String.join(KEY_SEPARATOR, sliceKey(keys, 1)));
        }

        throw new IllegalArgumentException("Storage '" + ns + "' not found");
    }

    @Override
    public void set(@NotNull String key, @Nullable Object value)
    {
        String[] keys = splitKey(key);
        if (keys.length <= 2)
            return;
        String ns = keys[0];

        for (ChildStorage storage : this.storages)
        {
            if (storage.getKey().equalsIgnoreCase(ns))
            {
                storage.set(String.join(KEY_SEPARATOR, sliceKey(keys, 1)), value);
                return;
            }
        }

        throw new IllegalArgumentException("Storage '" + ns + "' not found");
    }
}
