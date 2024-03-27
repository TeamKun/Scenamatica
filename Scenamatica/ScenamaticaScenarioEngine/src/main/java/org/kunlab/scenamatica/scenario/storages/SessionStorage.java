package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;

public class SessionStorage extends AbstractVariableProvider implements ChildStorage
{
    public SessionStorage(@NotNull ScenarioSession session, @NotNull ScenarioStorage scenarioStorage)
    {
        this.putAll(childMap(
                "started_at", func(keys -> session.getStartedAt()),
                "scenarios", func(keys -> {
                    if (keys.length == 0)
                        return session.getScenarios();
                    else
                        return scenarioStorage.getScenarioDetail(session, keys);
                })
        ));
    }

    @Override
    public String getKey()
    {
        return "session";
    }
}
