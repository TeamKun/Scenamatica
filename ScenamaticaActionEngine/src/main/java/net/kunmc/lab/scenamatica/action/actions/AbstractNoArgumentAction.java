package net.kunmc.lab.scenamatica.action.actions;

import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AbstractNoArgumentAction extends AbstractAction<AbstractNoArgumentAction.NoArgument>
{
    @Override
    public void execute(@Nullable NoArgument argument)
    {
        this.execute();
    }

    protected abstract void execute();

    protected abstract boolean isFired(@NotNull Plugin plugin, @NotNull Event event);

    @Override
    public boolean isFired(@NotNull NoArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        return this.isFired(plugin, event);
    }

    @Override
    public NoArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        return null;
    }

    public static class NoArgument implements ActionArgument
    {
        @Override
        public boolean isSame(TriggerArgument argument)
        {
            return argument instanceof NoArgument;
        }
    }
}
