package org.kunlab.scenamatica.action.actions.inventory;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Map;

public abstract class AbstractInventoryAction<A extends AbstractInventoryArgument> extends AbstractAction<A>
{
    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof InventoryEvent))
            return false;

        InventoryEvent e = (InventoryEvent) event;

        return BeanUtils.isSame(argument.getInventory(), e.getInventory(), true);
    }

    protected String deserializeInventory(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractInventoryArgument.KEY_INVENTORY);

        return map.get(AbstractInventoryArgument.KEY_INVENTORY).toString();
    }
}
