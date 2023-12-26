package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

public class InventoryClickAction extends AbstractInventoryInteractAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "inventory_click";
    public static final InputToken<ClickType> IN_CLICK_TYPE = ofInput(
            "type",
            ClickType.class,
            ofEnum(ClickType.class)
    );
    public static final InputToken<InventoryAction> IN_INVENTORY_ACTION = ofInput(
            "action",
            InventoryAction.class,
            ofEnum(InventoryAction.class)
    );
    public static final InputToken<InventoryType.SlotType> IN_SLOT_TYPE = ofInput(
            "slotType",
            InventoryType.SlotType.class,
            ofEnum(InventoryType.SlotType.class)
    );
    public static final InputToken<Integer> IN_SLOT = ofInput(
            "slot",
            Integer.class
    );
    public static final InputToken<Integer> IN_RAW_SLOT = ofInput(
            "rawSlot",
            Integer.class
    );
    public static final InputToken<ItemStackStructure> IN_CLICKED_ITEM = ofInput(
            "clickedItem",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final InputToken<Integer> IN_BUTTON = ofInput(
            "button",
            Integer.class
    );
    public static final InputToken<ItemStackStructure> IN_CURSOR_ITEM = ofInput(
            "cursorItem",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player target = argument.get(IN_PLAYER).selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalStateException("Target is not found."));
        Actor actor = PlayerUtils.getActorOrThrow(engine, target);
        ClickType type = argument.get(IN_CLICK_TYPE);

        if (argument.isPresent(IN_INVENTORY))
            target.openInventory(argument.get(IN_INVENTORY).create());

        Integer slot = argument.orElse(IN_SLOT, () -> {
            if (argument.orElse(IN_SLOT_TYPE, () -> null) == InventoryType.SlotType.OUTSIDE)
                return -999;
            else
                return target.getOpenInventory().convertSlot(argument.get(IN_RAW_SLOT));
        });

        Integer button = argument.orElse(IN_BUTTON, () -> {
            switch (type)
            {
                case LEFT:
                    return 0;
                case RIGHT:
                    return 1;
                case MIDDLE:
                    return 2;
                default:
                    throw new IllegalStateException("Invalid click type: " + type);
            }
        });

        assert button != null;

        ItemStack clicked = argument.ifPresent(IN_CLICKED_ITEM, ItemStackStructure::create, null);

        actor.clickInventory(
                type,
                slot,
                button,
                clicked
        );
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryInteractEvent(argument, engine, event))
            return false;

        InventoryClickEvent e = (InventoryClickEvent) event;

        return argument.ifPresent(IN_CLICK_TYPE, type -> type == e.getClick())
                && argument.ifPresent(IN_INVENTORY_ACTION, action -> action == e.getAction())
                && argument.ifPresent(IN_SLOT_TYPE, slotType -> slotType == e.getSlotType())
                && argument.ifPresent(IN_SLOT, slot -> slot == e.getSlot())
                && argument.ifPresent(IN_RAW_SLOT, rawSlot -> rawSlot == e.getRawSlot())
                && argument.ifPresent(IN_CLICKED_ITEM, clickedItem -> clickedItem.isAdequate(e.getCurrentItem()))
                && argument.ifPresent(IN_BUTTON, button -> button == e.getHotbarButton())
                && argument.ifPresent(IN_CURSOR_ITEM, cursorItem -> cursorItem.isAdequate(e.getCursor()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryClickEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_CLICK_TYPE, IN_INVENTORY_ACTION, IN_SLOT_TYPE,
                        IN_SLOT, IN_RAW_SLOT, IN_CLICKED_ITEM, IN_BUTTON, IN_CURSOR_ITEM
                );

        if (type == ScenarioType.ACTION_EXECUTE)
            board.validator(
                            b -> b.isPresent(IN_SLOT) || b.isPresent(IN_RAW_SLOT),
                            "slot or raw_slot must be present at the same time"
                    )
                    .validator(
                            b -> !(b.isPresent(IN_SLOT) && b.isPresent(IN_RAW_SLOT)),
                            "cannot specify both slot and raw_slot at the same time"
                    )
                    .validator(
                            b -> b.isPresent(IN_BUTTON) || !b.isPresent(IN_CLICK_TYPE)
                                    || (b.get(IN_CLICK_TYPE) == ClickType.LEFT || b.get(IN_CLICK_TYPE) == ClickType.RIGHT || b.get(IN_CLICK_TYPE) == ClickType.MIDDLE),
                            "button cannot be null when click type is not left, right or middle"
                    );

        return board;
    }
}
