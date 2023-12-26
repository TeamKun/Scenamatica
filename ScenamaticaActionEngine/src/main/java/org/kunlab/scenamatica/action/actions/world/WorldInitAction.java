package org.kunlab.scenamatica.action.actions.world;

import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

public class WorldInitAction extends AbstractWorldAction
        implements Watchable
{
    public static final String KEY_ACTION_NAME = "world_init";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldInitEvent.class
        );
    }
}
