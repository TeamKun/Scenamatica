package org.kunlab.scenamatica.action.actions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Map;

public abstract class AbstractNoArgumentAction extends AbstractAction<AbstractNoArgumentAction.NoArgument>
{
    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable NoArgument argument)
    {
        this.execute();
    }

    protected abstract void execute();

    protected abstract boolean isFired(@NotNull ScenarioEngine engine, @NotNull Event event);

    @Override
    public boolean isFired(@NotNull NoArgument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        return this.isFired(engine, event);
    }

    @Override
    public NoArgument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return null;
    }

    public static class NoArgument extends AbstractActionArgument
    {
        @Override
        public boolean isSame(TriggerArgument argument)
        {
            return argument instanceof NoArgument;
        }

        @Override
        public String getArgumentString()
        {
            return "";
        }
    }
}
