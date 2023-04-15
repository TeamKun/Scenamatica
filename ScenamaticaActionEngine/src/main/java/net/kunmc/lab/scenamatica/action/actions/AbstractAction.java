package net.kunmc.lab.scenamatica.action.actions;

import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAction<A extends ActionArgument> implements Action<A>
{
    @Override
    public void onStartWatching(@Nullable A argument, @NotNull Plugin plugin, @Nullable Event event)
    {
    }

    @NotNull
    protected A requireArgsNonNull(@Nullable A argument)
    {
        if (argument == null)
            throw new IllegalArgumentException("Cannot execute action without argument.");

        return argument;
    }

}
