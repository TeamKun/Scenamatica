package org.kunlab.scenamatica.action.actions.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;

public class InventoryCloseAction extends AbstractInventoryAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "inventory_close";
    public static final InputToken<PlayerSpecifier> IN_PLAYER = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );
    public static final InputToken<InventoryCloseEvent.Reason> IN_REASON = ofInput(
            "reason",
            InventoryCloseEvent.Reason.class,
            ofEnum(InventoryCloseEvent.Reason.class)
    );

    public static final String KEY_OUT_TARGET = "target";
    public static final String KEY_OUT_REASON = "reason";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = ctxt.input(IN_PLAYER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));

        InventoryCloseEvent.Reason reason = ctxt.orElseInput(IN_REASON, () -> null);
        if (reason == null)
            player.closeInventory();
        else
            player.closeInventory(reason);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(ctxt, event))
            return false;

        assert event instanceof InventoryCloseEvent;
        InventoryCloseEvent e = (InventoryCloseEvent) event;
        HumanEntity player = e.getPlayer();
        if (!(player instanceof Player))
            return false;

        boolean result = ctxt.ifHasInput(IN_PLAYER, specifier -> specifier.checkMatchedPlayer((Player) player))
                && ctxt.ifHasInput(IN_REASON, reason -> reason == e.getReason());
        if (result)
            this.makeOutputs(ctxt, e);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Inventory inventory, @NotNull Player player, @Nullable InventoryCloseEvent.Reason reason)
    {
        ctxt.output(KEY_OUT_TARGET, player);
        if (reason != null)
            ctxt.output(KEY_OUT_REASON, reason);
        super.makeOutputs(ctxt, inventory);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull InventoryCloseEvent e)
    {
        this.makeOutputs(ctxt, e.getInventory(), (Player) e.getPlayer(), e.getReason());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryCloseEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_PLAYER, IN_REASON);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_PLAYER);
        else
            board.register(IN_INVENTORY); // EXECUTE には INVENTORY は必要ない

        return board;
    }
}
