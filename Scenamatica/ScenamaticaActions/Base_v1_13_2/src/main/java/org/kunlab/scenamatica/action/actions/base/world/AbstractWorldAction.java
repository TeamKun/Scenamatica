package org.kunlab.scenamatica.action.actions.base.world;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

@Category(
        id = "world",
        name = "ワールド",
        description = "ワールドに関するアクションを提供します。"
)
@OutputDocs({
        @OutputDoc(
                name = AbstractWorldAction.KEY_OUT_WORLD,
                description = "ワールドの名前です。",
                type = String.class
        )
})
public abstract class AbstractWorldAction extends AbstractAction
        implements Watchable
{
    @InputDoc(
            name = "world",
            description = "ワールドの名前です。",
            type = String.class
    )
    public static final InputToken<String> IN_WORLD = ofInput(
            "world",
            String.class
    );
    public static final String KEY_OUT_WORLD = "world";
    private static final String[] PADDING_TARGET = {"the_end", "nether"};

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof WorldEvent))
            return false;

        WorldEvent e = (WorldEvent) event;

        boolean result = ctxt.ifHasInput(IN_WORLD, world ->
                world.equals(e.getWorld().getName())
                || ("world_" + world).equals(e.getWorld().getName()));
        this.makeOutputs(ctxt, e.getWorld().getName());
        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return ofInputs(type, IN_WORLD);
    }

    protected World getWorld(ActionContext ctxt)
    {
        String name = ctxt.input(IN_WORLD);
        World world = null;
        if (name == null || (world = Bukkit.getWorld(name)) != null)
            return world;

        if (ArrayUtils.contains(PADDING_TARGET, name))
            return Bukkit.getWorld("world_" + name);

        return null;
    }

    protected World getWorldNonNull(ActionContext ctxt)
    {
        if (!ctxt.hasInput(IN_WORLD))
            return ctxt.getContext().getStage().getWorld();

        return this.getWorld(ctxt);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull String worldName)
    {
        ctxt.output(KEY_OUT_WORLD, worldName);
        ctxt.commitOutput();
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world)
    {
        this.makeOutputs(ctxt, world.getName());
    }
}
