package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

public class InventoryCreativeAction extends InventoryClickAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "inventory_creative";
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        int slot = ctxt.input(IN_SLOT);
        Actor actor = ctxt.getActorOrThrow(ctxt.input(IN_PLAYER)
                .selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Target is not found."))
        );

        actor.giveCreativeItem(slot, ctxt.input(IN_ITEM).create());
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        InventoryCreativeEvent e = (InventoryCreativeEvent) event;

        return super.checkFired(ctxt, event)
                && ctxt.ifHasInput(IN_ITEM, item -> item.isAdequate(e.getCursor()));
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
