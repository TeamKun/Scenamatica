package org.kunlab.scenamatica.action.actions.world;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.world.border.WorldBorderAction;
import org.kunlab.scenamatica.action.actions.world.border.WorldBorderChangedAction;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof WorldEvent))
            return false;

        WorldEvent e = (WorldEvent) event;

        return e.getWorld().getKey().equals(this.getWorldNonNull(argument, engine).getKey());
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return ofInputs(type, IN_WORLD);
    }

    public World getWorld(InputBoard argument)
    {
        NamespacedKey key = argument.get(IN_WORLD);
        World world = null;
        if (key == null || (world = Bukkit.getWorld(key)) != null)
            return world;

        if (ArrayUtils.contains(PADDING_TARGET, key.getKey()))
            return Bukkit.getWorld(NamespaceUtils.fromString(key.getNamespace() + ":" + "world_" + key.getKey()));

        return null;
    }

    public World getWorldNonNull(InputBoard argument, ScenarioEngine engine)
    {
        if (!argument.isPresent(IN_WORLD))
            return engine.getContext().getStage().getWorld();

        return this.getWorld(argument);
    }
}
