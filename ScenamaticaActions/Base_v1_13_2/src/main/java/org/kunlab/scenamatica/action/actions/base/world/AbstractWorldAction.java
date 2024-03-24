package org.kunlab.scenamatica.action.actions.base.world;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.action.actions.base.world.border.WorldBorderAction;
import org.kunlab.scenamatica.action.actions.base.world.border.WorldBorderChangedAction;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWorldAction extends AbstractAction
        implements Watchable
{
    public static final String KEY_WORLD = "world";
    public static final InputToken<NamespacedKey> IN_WORLD = ofInput(
            KEY_WORLD,
            NamespacedKey.class,
            ofTraverser(String.class, (ser, str) -> NamespaceUtils.fromString(str))
    );
    public static final String KEY_OUT_WORLD = "world";
    private static final String[] PADDING_TARGET = {"the_end", "nether"};

    public static List<? extends AbstractWorldAction> getActions()
    {
        List<AbstractWorldAction> actions = new ArrayList<>();

        actions.add(new WorldBorderAction());
        actions.add(new WorldBorderChangedAction());
        actions.add(new WorldGameRuleAction());
        actions.add(new WorldInitAction());
        actions.add(new WorldLoadAction());
        actions.add(new WorldSaveAction());
        actions.add(new WorldUnloadAction());

        return actions;
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof WorldEvent))
            return false;

        WorldEvent e = (WorldEvent) event;

        boolean result = ctxt.ifHasInput(IN_WORLD, world -> world.equals(e.getWorld().getKey()));
        this.makeOutputs(ctxt, e.getWorld().getKey());
        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return ofInputs(type, IN_WORLD);
    }

    protected World getWorld(ActionContext ctxt)
    {
        NamespacedKey key = ctxt.input(IN_WORLD);
        World world = null;
        if (key == null || (world = Bukkit.getWorld(key)) != null)
            return world;

        if (ArrayUtils.contains(PADDING_TARGET, key.getKey()))
            return Bukkit.getWorld(NamespaceUtils.fromString(key.getNamespace() + ":" + "world_" + key.getKey()));

        return null;
    }

    protected World getWorldNonNull(ActionContext ctxt)
    {
        if (!ctxt.hasInput(IN_WORLD))
            return ctxt.getContext().getStage().getWorld();

        return this.getWorld(ctxt);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull NamespacedKey world)
    {
        ctxt.output(KEY_OUT_WORLD, world.getKey());
        ctxt.commitOutput();
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world)
    {
        this.makeOutputs(ctxt, world.getKey());
    }
}
