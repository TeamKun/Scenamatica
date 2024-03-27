package org.kunlab.scenamatica.action.actions.base.world;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

public abstract class AbstractWorldAction extends AbstractAction
        implements Watchable
{
    public static final String KEY_WORLD = "world";
    public static final InputToken<String> IN_WORLD = ofInput(
            KEY_WORLD,
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

        boolean result = ctxt.ifHasInput(IN_WORLD, world -> world.equals(e.getWorld().getName()));
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
