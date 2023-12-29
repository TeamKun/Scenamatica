package org.kunlab.scenamatica.action.actions.world;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

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
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = super.getWorldNonNull(ctxt);
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
