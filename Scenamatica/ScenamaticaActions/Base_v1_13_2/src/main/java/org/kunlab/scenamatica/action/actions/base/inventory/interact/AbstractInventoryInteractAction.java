package org.kunlab.scenamatica.action.actions.base.inventory.interact;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.base.inventory.AbstractInventoryAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

public abstract class AbstractInventoryInteractAction
        extends AbstractInventoryAction
{
    public static final InputToken<PlayerSpecifier> IN_PLAYER = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );

    public static final String OUT_KEY_TARGET = "target";

    public boolean checkMatchedInventoryInteractEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(ctxt, event))
            return false;

        assert event instanceof InventoryInteractEvent;
        InventoryInteractEvent e = (InventoryInteractEvent) event;
        HumanEntity whoClicked = e.getWhoClicked();
        if (!(whoClicked instanceof Player))
            return false;

        return ctxt.ifHasInput(IN_PLAYER, player -> player.checkMatchedPlayer((Player) whoClicked));
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull HumanEntity player, @Nullable Inventory inventory)
    {
        ctxt.output(OUT_KEY_TARGET, player);
        super.makeOutputs(ctxt, inventory);
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
