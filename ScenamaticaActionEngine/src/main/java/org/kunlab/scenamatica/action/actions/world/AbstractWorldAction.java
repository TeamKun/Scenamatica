package org.kunlab.scenamatica.action.actions.world;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.world.border.WorldBorderAction;
import org.kunlab.scenamatica.action.actions.world.border.WorldBorderChangedAction;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractWorldAction<A extends AbstractWorldActionArgument>
        extends AbstractAction<A>
        implements Watchable<A>
{
    public static List<? extends AbstractWorldAction<?>> getActions()
    {
        List<AbstractWorldAction<?>> actions = new ArrayList<>();

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
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof WorldEvent))
            return false;

        WorldEvent e = (WorldEvent) event;

        return e.getWorld().getKey().equals(argument.getWorldNonNull(engine).getKey());
    }

    protected NamespacedKey deserializeWorld(Map<String, Object> map)
    {
        if (map.containsKey(AbstractWorldActionArgument.KEY_WORLD))
            return NamespaceUtils.fromString(map.get(AbstractWorldActionArgument.KEY_WORLD).toString());

        return null;
    }
}
