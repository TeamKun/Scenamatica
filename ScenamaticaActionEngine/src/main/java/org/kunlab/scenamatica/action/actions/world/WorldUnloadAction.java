package org.kunlab.scenamatica.action.actions.world;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.util.Collections;
import java.util.List;

public class WorldUnloadAction extends AbstractWorldAction
        implements Executable, Requireable
{
    public static final String KEY_ACTION_NAME = "world_unload";
    public static final InputToken<Boolean> IN_SAVE = ofInput(
            "save",
            Boolean.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = super.getWorldNonNull(ctxt);

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
        NamespacedKey key = ctxt.input(IN_WORLD);
        assert key != null;

        boolean result = Bukkit.getWorld(key) == null;
        if (result)
            this.makeOutputs(ctxt, key);
        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_SAVE);
    }
}
