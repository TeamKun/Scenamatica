package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractInventoryInteractAction<A extends AbstractInventoryInteractArgument>
        extends AbstractInventoryAction<A>
{
    public static List<? extends AbstractInventoryInteractAction<?>> getActions()
    {
        List<AbstractInventoryInteractAction<?>> actions = new ArrayList<>();

        actions.add(new InventoryClickAction());

        return actions;
    }

    public boolean checkMatchedInventoryInteractEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(argument, engine, event))
            return false;
        else if (!(event instanceof InventoryInteractEvent))
            return false;

        InventoryInteractEvent e = (InventoryInteractEvent) event;

        return e.getWhoClicked().getUniqueId().equals(argument.getTarget().getUniqueId());
    }

    protected String deserializeTarget(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractInventoryInteractArgument.KEY_TARGET_PLAYER);

        return map.get(AbstractInventoryInteractArgument.KEY_TARGET_PLAYER).toString();
    }
}
