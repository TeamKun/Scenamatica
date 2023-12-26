package org.kunlab.scenamatica.action.actions.world;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        World world = super.getWorldNonNull(argument, engine);

        Bukkit.getServer().unloadWorld(world, argument.orElse(IN_SAVE, () -> true));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldUnloadEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        NamespacedKey key = argument.get(IN_WORLD);
        assert key != null;

        return Bukkit.getWorld(key) == null;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_SAVE);

        return board;
    }
}
