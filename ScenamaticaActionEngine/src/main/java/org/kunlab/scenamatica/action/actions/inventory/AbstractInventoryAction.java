package org.kunlab.scenamatica.action.actions.inventory;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.inventory.interact.AbstractInventoryInteractAction;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractInventoryAction<A extends AbstractInventoryArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractInventoryAction<?>> getActions()
    {
        List<AbstractInventoryAction<?>> actions = new ArrayList<>(AbstractInventoryInteractAction.getActions());

        actions.add(new InventoryCloseAction());
        actions.add(new InventoryOpenAction());

        return actions;
    }

    public boolean checkMatchedInventoryEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof InventoryEvent))
            return false;

        if (argument.getInventory() == null)
            return true;

        InventoryEvent e = (InventoryEvent) event;

        return BeanUtils.isSame(argument.getInventory(), e.getInventory(), false);
    }

    protected InventoryBean deserializeInventoryIfContains(Map<String, Object> map, BeanSerializer serializer)
    {
        if (!map.containsKey(AbstractInventoryArgument.KEY_INVENTORY))
            return null;

        return serializer.deserialize(
                MapUtils.checkAndCastMap(AbstractInventoryArgument.KEY_INVENTORY),
                InventoryBean.class
        );
    }
}
