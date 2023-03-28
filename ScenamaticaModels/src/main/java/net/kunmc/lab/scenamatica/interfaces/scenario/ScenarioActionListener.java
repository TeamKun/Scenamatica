package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.WatchingEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public interface ScenarioActionListener
{
    <A extends ActionArgument> void onActionError(@NotNull CompiledAction<A> action, @NotNull Throwable error);

    <A extends ActionArgument> void onActionExecuted(@NotNull CompiledAction<A> action);

    <A extends ActionArgument> void onActionFired(@NotNull WatchingEntry<A> entry, @NotNull Event event);
}
