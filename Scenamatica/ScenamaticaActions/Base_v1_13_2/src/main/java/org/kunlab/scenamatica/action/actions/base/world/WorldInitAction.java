package org.kunlab.scenamatica.action.actions.base.world;

import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@Action("world_init")
public class WorldInitAction extends AbstractWorldAction
        implements Watchable
{

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldInitEvent.class
        );
    }
}
