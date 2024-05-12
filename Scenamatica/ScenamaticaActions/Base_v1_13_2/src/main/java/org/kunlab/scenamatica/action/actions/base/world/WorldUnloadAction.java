package org.kunlab.scenamatica.action.actions.base.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.util.Collections;
import java.util.List;

@Action("world_unload")
public class WorldUnloadAction extends AbstractWorldAction
        implements Executable, Requireable
{
    public static final InputToken<Boolean> IN_SAVE = ofInput(
            "save",
            Boolean.class
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = super.getWorld(ctxt);

        if (world == null)
            throw new IllegalStateException("Unable to find world: " + ctxt.input(IN_WORLD));
        this.makeOutputs(ctxt, world);
        Bukkit.getServer().unloadWorld(world, ctxt.orElseInput(IN_SAVE, () -> true));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldUnloadEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        String worldName = ctxt.input(IN_WORLD);

        boolean result = Bukkit.getWorld(worldName) == null;
        if (result)
            this.makeOutputs(ctxt, worldName);
        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_SAVE);
    }
}
