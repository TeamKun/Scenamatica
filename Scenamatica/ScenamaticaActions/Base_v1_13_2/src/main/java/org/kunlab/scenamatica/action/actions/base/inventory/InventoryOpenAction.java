package org.kunlab.scenamatica.action.actions.base.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;

@ActionMeta("inventory_open")
public class InventoryOpenAction extends AbstractInventoryAction
        implements Executable, Watchable
{
    public static final InputToken<PlayerSpecifier> IN_PLAYER = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );
    public static final String KEY_OUT_TARGET = "target";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = ctxt.input(IN_PLAYER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));

        InventoryStructure inventoryStructure = ctxt.input(IN_INVENTORY);
        Inventory inventory = inventoryStructure.create();

        this.makeOutputs(ctxt, player, inventory);
        player.openInventory(inventory);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(ctxt, event))
            return false;

        assert event instanceof InventoryOpenEvent;
        InventoryOpenEvent e = (InventoryOpenEvent) event;
        HumanEntity player = e.getPlayer();
        if (!(player instanceof Player))
            return false;

        boolean result = ctxt.ifHasInput(IN_PLAYER, playerSpecifier -> playerSpecifier.checkMatchedPlayer((Player) player));
        if (result)
            this.makeOutputs(ctxt, e);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Inventory inventory)
    {
        ctxt.output(KEY_OUT_TARGET, player);
        super.makeOutputs(ctxt, inventory);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull InventoryOpenEvent event)
    {
        this.makeOutputs(ctxt, (Player) event.getPlayer(), event.getInventory());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryOpenEvent.class
        );
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
