package org.kunlab.scenamatica.scenario;

import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioManager;
import org.kunlab.scenamatica.interfaces.scenario.SessionCreator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionCreatorImpl implements SessionCreator
{
    private final ScenarioManager manager;
    private final List<SessionElement> sessions;

    public SessionCreatorImpl(ScenarioManager manager)
    {
        this.manager = manager;
        this.sessions = new ArrayList<>();
    }

    @Override
    public SessionCreator add(@NotNull Plugin plugin, @NotNull TriggerType trigger, @NotNull String name)
    {
        this.sessions.add(new SessionElement(plugin, name, trigger));
        return this;
    }

    @Override
    public SessionCreator add(@NotNull ScenarioEngine engine, @NotNull TriggerType trigger)
    {
        this.sessions.add(new SessionElement(engine, trigger));
        return this;
    }

    @Override
    public void queueAll() throws TriggerNotFoundException, ScenarioNotFoundException
    {
        this.manager.queueScenario(this);
    }

    @Override
    public List<SessionElement> getSessions()
    {
        return Collections.unmodifiableList(this.sessions);
    }

    @Override
    public boolean isEmpty()
    {
        return this.sessions.isEmpty();
    }
}
