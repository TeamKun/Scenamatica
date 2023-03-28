package net.kunmc.lab.scenamatica.scenario;

import lombok.RequiredArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.WatchingEntry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ScenarioActionListenerImpl implements ScenarioActionListener
{
    private final ScenarioEngine engine;

    @Override
    public <A extends ActionArgument> void onActionError(@NotNull CompiledAction<A> action, @NotNull Throwable error)
    {

    }

    @Override
    public <A extends ActionArgument> void onActionExecuted(@NotNull CompiledAction<A> action)
    {

    }

    @Override
    public <A extends ActionArgument> void onActionFired(@NotNull WatchingEntry<A> entry, @NotNull Event event)
    {

    }
}
