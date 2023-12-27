package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;

public class ScenarioStorage extends AbstractVariableProvider implements ChildStorage
{

    private final ScenarioSession session;
    private final SessionStorage storage;

    public ScenarioStorage(@NotNull ScenarioSession session, @NotNull SessionStorage sessionStorage)
    {
        super(null);
        this.session = session;
        this.storage = sessionStorage;
    }

    @Override
    public @Nullable Object get(@NotNull String key)
    {
        QueuedScenario current = this.session.getCurrent();
        assert current != null;

        return this.storage.getScenarioDetail(current, splitKey(key));
    }

    @Override
    public String getKey()
    {
        return "scenario";
    }
}
