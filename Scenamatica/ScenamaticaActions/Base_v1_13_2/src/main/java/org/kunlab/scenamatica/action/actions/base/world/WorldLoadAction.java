package org.kunlab.scenamatica.action.actions.base.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Action("world_load")
@ActionDoc(
        name = "ワールド読み込み",
        description = "ワールドを読み込みます。",
        events = {
                WorldLoadEvent.class
        },

        executable = "ワールドを読み込みます。",
        expectable = "ワールドが読み込まれることを期待します。",
        requireable = "ワールドが読み込まれていることを要求します。"
)
public class WorldLoadAction extends AbstractWorldAction
        implements Executable, Requireable
{

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        String worldName = ctxt.input(IN_WORLD);
        assert worldName != null;

        Path worldDir = Bukkit.getWorldContainer().toPath();
        if (!Files.exists(worldDir.resolve(worldName)))
            if (Files.exists(worldDir.resolve("world_" + worldName)))
                worldName = "world_" + worldName;
            else
                throw new IllegalActionInputException(IN_WORLD, "World '" + worldName + "' does not exist.");

        // createWorld は, ワールドが存在する場合は読み込むだけ。
        this.makeOutputs(ctxt, worldName);
        Bukkit.createWorld(new WorldCreator(worldName));
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
