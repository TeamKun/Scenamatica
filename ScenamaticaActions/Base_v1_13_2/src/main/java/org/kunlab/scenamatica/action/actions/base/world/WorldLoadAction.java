package org.kunlab.scenamatica.action.actions.base.world;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@ActionMeta("world_load")
public class WorldLoadAction extends AbstractWorldAction
        implements Executable, Requireable
{

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        NamespacedKey key = ctxt.input(IN_WORLD);
        assert key != null;

        Path worldDir = Bukkit.getWorldContainer().toPath();
        if (!Files.exists(worldDir.resolve(key.getKey())))
            if (Files.exists(worldDir.resolve("world_" + key.getKey())))
                key = NamespacedKey.fromString(key.getNamespace() + ":world_" + key.getKey());
            else
                throw new IllegalArgumentException("World '" + key.getKey() + "' does not exist.");
        assert key != null;

        // createWorld は, ワールドが存在する場合は読み込むだけ。
        this.makeOutputs(ctxt, key);
        Bukkit.createWorld(new WorldCreator(key.getKey()));
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        World world;
        boolean result = (world = super.getWorld(ctxt)) != null;
        if (result)
            this.makeOutputs(ctxt, world);
        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldLoadEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_WORLD);
        return board;
    }
}
