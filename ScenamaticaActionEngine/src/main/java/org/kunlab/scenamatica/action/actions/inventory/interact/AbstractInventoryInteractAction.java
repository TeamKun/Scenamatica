package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryAction;
import org.kunlab.scenamatica.commons.specifiers.PlayerSpecifierImpl;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractInventoryInteractAction<A extends AbstractInventoryInteractArgument>
        extends AbstractInventoryAction<A>
{
    public static List<? extends AbstractInventoryInteractAction<?>> getActions()
    {
        List<AbstractInventoryInteractAction<?>> actions = new ArrayList<>();

        actions.add(new InventoryClickAction<>());
        actions.add(new InventoryCreativeAction());

        return actions;
    }

    public boolean checkMatchedInventoryInteractEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(argument, engine, event))
            return false;
        else if (!(event instanceof InventoryInteractEvent))
            return false;

        InventoryInteractEvent e = (InventoryInteractEvent) event;
        HumanEntity whoClicked = e.getWhoClicked();

        return (!argument.getTargetSpecifier().canProvideTarget()
                || argument.getTargetSpecifier().checkMatchedPlayer((Player) whoClicked));
    }

    @NotNull
    protected PlayerSpecifier deserializeTarget(Map<String, Object> map, StructureSerializer serializer)
    {
        return PlayerSpecifierImpl.tryDeserializePlayer(
                map.get(AbstractInventoryInteractArgument.KEY_TARGET_PLAYER),
                serializer
        );
    }
}
