package org.kunlab.scenamatica.action.actions.base.world;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@Action("world_save")
@ActionDoc(
        name = "ワールド保存",
        description = "ワールドを保存します。",
        events = {
                WorldSaveEvent.class
        },

        executable = "ワールドを保存します。",
        watchable = "ワールドが保存されることを期待します。",
        requireable = ActionDoc.UNALLOWED

)
public class WorldSaveAction extends AbstractWorldAction
        implements Executable, Watchable
{

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = super.getWorldNonNull(ctxt);
        this.makeOutputs(ctxt, world);
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
