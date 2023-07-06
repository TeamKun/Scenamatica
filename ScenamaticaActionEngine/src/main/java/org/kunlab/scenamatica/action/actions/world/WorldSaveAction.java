package org.kunlab.scenamatica.action.actions.world;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorldSaveAction extends AbstractWorldAction<WorldSaveAction.Argument>
{
    public static final String KEY_ACTION_NAME = "world_save";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = super.requireArgsNonNull(argument);
        World world = argument.getWorldNonNull(engine);
        world.save();
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldSaveEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeWorld(map)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractWorldActionArgument
    {
        public Argument(@Nullable NamespacedKey worldRef)
        {
            super(worldRef);
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return this.isSameWorld(arg);
        }

        @Override
        public String getArgumentString()
        {
            return super.getArgumentString();
        }
    }
}
