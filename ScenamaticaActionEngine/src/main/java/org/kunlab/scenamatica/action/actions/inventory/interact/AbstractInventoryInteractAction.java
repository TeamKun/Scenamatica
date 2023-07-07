package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Map;

public abstract class AbstractInventoryInteractAction<A extends AbstractInventoryInteractArgument>
        extends AbstractInventoryAction<A>
{
    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
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
