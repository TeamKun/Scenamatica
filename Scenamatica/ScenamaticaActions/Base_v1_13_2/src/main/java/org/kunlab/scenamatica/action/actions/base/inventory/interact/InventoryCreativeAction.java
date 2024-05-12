package org.kunlab.scenamatica.action.actions.base.inventory.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

@Action("inventory_creative")
public class InventoryCreativeAction extends InventoryClickAction
        implements Executable, Watchable
{
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final String KEY_OUT_ITEM = "item";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        int slot = ctxt.input(IN_SLOT);
        Actor actor = ctxt.getActorOrThrow(ctxt.input(IN_PLAYER)
                .selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Target is not found."))
        );

        ItemStack stack = ctxt.input(IN_ITEM).create();
        this.makeOutputs(ctxt, actor.getPlayer(), stack, slot);
        actor.giveCreativeItem(slot, stack);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull ItemStack item, int slot)
    {
        ctxt.output(KEY_OUT_ITEM, item);
        ctxt.output(OUT_KEY_SLOT, slot);
        super.makeOutputs(ctxt, player.getInventory());
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        assert event instanceof InventoryCreativeEvent;
        InventoryCreativeEvent e = (InventoryCreativeEvent) event;

        boolean result = super.checkFired(ctxt, event)
                && ctxt.ifHasInput(IN_ITEM, item -> item.isAdequate(e.getCursor()));
        if (result)
            ctxt.output(KEY_OUT_ITEM, e.getCursor());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull InventoryCreativeEvent e)
    {
        ctxt.output(KEY_OUT_ITEM, e.getCursor());
        super.makeOutputs(ctxt, e);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryCreativeEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ITEM);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ITEM, IN_SLOT);

        return board;
    }
}
