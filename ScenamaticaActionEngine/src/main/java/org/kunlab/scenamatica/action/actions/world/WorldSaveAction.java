package org.kunlab.scenamatica.action.actions.world;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

public class WorldSaveAction extends AbstractWorldAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "world_save";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        World world = super.getWorldNonNull(argument, engine);
        world.save();
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldSaveEvent.class
        );
    }
}
