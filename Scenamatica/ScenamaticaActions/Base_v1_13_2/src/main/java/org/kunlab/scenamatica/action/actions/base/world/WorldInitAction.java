package org.kunlab.scenamatica.action.actions.base.world;

import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@Action("world_init")
@ActionDoc(
        name = "ワールド初期化",
        description = "ワールドの初期化に関するアクションです。",
        events = {
                WorldInitEvent.class
        },

        executable = ActionDoc.UNALLOWED,
        watchable = "ワールドの初期化が実行されることを期待します。",
        requireable = ActionDoc.UNALLOWED
)
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
