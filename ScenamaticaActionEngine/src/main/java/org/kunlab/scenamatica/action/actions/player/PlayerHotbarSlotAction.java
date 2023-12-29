package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

public class PlayerHotbarSlotAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "player_hotbar";

    public static final InputToken<Integer> IN_CURRENT_SLOT = ofInput(
            "slot",
            Integer.class
    ).validator(value -> value >= 0 && value <= 8, "Slot must be between 0 and 8");
    public static final InputToken<Integer> IN_PREVIOUS_SLOT = ofInput(
            "previous",
            Integer.class
    ).validator(value -> value >= 0 && value <= 8, "Previous slot must be between 0 and 8");
    public static final InputToken<ItemStackStructure> IN_CURRENT_ITEM = ofInput(
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
        Player p = selectTarget(ctxt);
        int slot = ctxt.input(IN_CURRENT_SLOT);

        p.getInventory().setHeldItemSlot(slot);
        ctxt.runIfHasInput(IN_CURRENT_ITEM, item -> p.getInventory().setItemInMainHand(item.create()));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerItemHeldEvent;
        PlayerItemHeldEvent e = (PlayerItemHeldEvent) event;

        int currentSlot = e.getNewSlot();
        int previousSlot = e.getPreviousSlot();
        ItemStack currentItem = e.getPlayer().getInventory().getItem(currentSlot);

        return ctxt.ifHasInput(IN_CURRENT_SLOT, slot -> slot == currentSlot)
                && ctxt.ifHasInput(IN_PREVIOUS_SLOT, slot -> slot == previousSlot)
                && ctxt.ifHasInput(IN_CURRENT_ITEM, item -> item.isAdequate(currentItem));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemHeldEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player p = selectTarget(ctxt);

        return ctxt.ifHasInput(IN_CURRENT_SLOT, slot -> slot == p.getInventory().getHeldItemSlot())
                && ctxt.ifHasInput(IN_CURRENT_ITEM, item -> item.isAdequate(p.getInventory().getItemInMainHand()));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_CURRENT_SLOT, IN_CURRENT_ITEM);

        if (type == ScenarioType.ACTION_EXPECT || type == ScenarioType.ACTION_EXECUTE)
            board.registerAll(IN_PREVIOUS_SLOT);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_CURRENT_SLOT);

        return board;
    }
}
