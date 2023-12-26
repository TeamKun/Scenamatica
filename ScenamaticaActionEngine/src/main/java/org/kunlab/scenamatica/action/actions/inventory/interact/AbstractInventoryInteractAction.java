package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInventoryInteractAction
        extends AbstractInventoryAction
{
    public static final InputToken<PlayerSpecifier> IN_PLAYER = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );

    public static List<? extends AbstractInventoryInteractAction> getActions()
    {
        List<AbstractInventoryInteractAction> actions = new ArrayList<>();

        actions.add(new InventoryClickAction());
        actions.add(new InventoryCreativeAction());

        return actions;
    }

    public boolean checkMatchedInventoryInteractEvent(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(argument, engine, event))
            return false;
        else if (!(event instanceof InventoryInteractEvent))
            return false;

        InventoryInteractEvent e = (InventoryInteractEvent) event;
        HumanEntity whoClicked = e.getWhoClicked();
        if (!(whoClicked instanceof Player))
            return false;

        return argument.ifPresent(IN_PLAYER, player -> player.checkMatchedEntity(whoClicked));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_PLAYER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_PLAYER);

        return board;
    }
}
