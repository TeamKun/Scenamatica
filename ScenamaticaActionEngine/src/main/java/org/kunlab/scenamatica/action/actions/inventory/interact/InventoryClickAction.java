package org.kunlab.scenamatica.action.actions.inventory.interact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryClickAction<T extends InventoryClickAction.Argument> extends AbstractInventoryInteractAction<T>
        implements Executable<T>, Watchable<T>
{
    public static final String KEY_ACTION_NAME = "inventory_click";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player target = argument.getTarget();
        Actor actor = PlayerUtils.getActorOrThrow(engine, target);

        if (argument.getInventory() != null)
            target.openInventory(argument.getInventory().createInventory());

        Integer slot = argument.getSlot();
        if (slot == null)
            if (argument.getSlotType() == InventoryType.SlotType.OUTSIDE)
                slot = -999;
            else
            {
                assert argument.getRawSlot() != null;
                slot = target.getOpenInventory().convertSlot(argument.getRawSlot());
            }

        Integer button = argument.getButton();
        if (button == null)
            if (argument.getType() == ClickType.LEFT)
                button = 0;
            else if (argument.getType() == ClickType.RIGHT)
                button = 1;
            else if (argument.getType() == ClickType.MIDDLE)
                button = 2;

        assert button != null;

        ItemStack clicked = null;
        if (argument.getClickedItem() != null)
            clicked = argument.getClickedItem().toItemStack();

        actor.clickInventory(
                argument.getType(),
                slot,
                button,
                clicked
        );
    }

    @Override
    public boolean isFired(@NotNull T argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryInteractEvent(argument, engine, event))
            return false;

        InventoryClickEvent e = (InventoryClickEvent) event;

        return (argument.getType() == null || argument.getType() == e.getClick()
                && (argument.getAction() == null || argument.getAction() == e.getAction()))
                && (argument.getSlotType() == null || argument.getSlotType() == e.getSlotType())
                && (argument.getSlot() == null || argument.getSlot() == e.getSlot())
                && (argument.getRawSlot() == null || argument.getRawSlot() == e.getRawSlot())
                && (argument.getClickedItem() == null || BeanUtils.isSame(argument.getClickedItem(), e.getCurrentItem(), false))
                && (argument.getButton() == null || argument.getButton() == e.getHotbarButton())
                && (argument.getCursorItem() == null || BeanUtils.isSame(argument.getCursorItem(), e.getCursor(), false));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryClickEvent.class
        );
    }

    @Override
    public T deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        ItemStackBean clickedItem = null;
        if (map.containsKey(Argument.KEY_CLICKED_ITEM))
            clickedItem = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(Argument.KEY_CLICKED_ITEM),
                    String.class,
                    Object.class
            ));

        ItemStackBean cursorItem = null;
        if (map.containsKey(Argument.KEY_CURSOR_ITEM))
            cursorItem = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(Argument.KEY_CURSOR_ITEM),
                    String.class,
                    Object.class
            ));

        // noinspection unchecked
        return (T) new Argument(
                super.deserializeInventoryIfContains(map, serializer),
                super.deserializeTarget(map),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_CLICK_TYPE, ClickType.class),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_INVENTORY_ACTION, InventoryAction.class),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_SLOT_TYPE, InventoryType.SlotType.class),
                MapUtils.getOrNull(map, Argument.KEY_SLOT),
                MapUtils.getOrNull(map, Argument.KEY_RAW_SLOT),
                clickedItem,
                MapUtils.getOrNull(map, Argument.KEY_CURSOR_ITEM),
                cursorItem
        );
    }

    @Getter // 継承用
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractInventoryInteractArgument
    {
        public static final String KEY_CLICK_TYPE = "type";
        public static final String KEY_INVENTORY_ACTION = "action";
        public static final String KEY_SLOT_TYPE = "slotType";
        public static final String KEY_SLOT = "slot";
        public static final String KEY_RAW_SLOT = "rawSlot";
        public static final String KEY_CLICKED_ITEM = "clickedItem";
        public static final String KEY_BUTTON = "button";
        public static final String KEY_CURSOR_ITEM = "cursorItem";

        ClickType type;
        InventoryAction action;
        InventoryType.SlotType slotType;
        Integer slot;
        Integer rawSlot;
        ItemStackBean clickedItem;
        Integer button;
        ItemStackBean cursorItem;

        public Argument(@Nullable InventoryBean inventory, @NotNull String target, ClickType type, InventoryAction action, InventoryType.SlotType slotType, Integer slot, Integer rawSlot, ItemStackBean clickedItem, Integer button, ItemStackBean cursorItem)
        {
            super(inventory, target);
            this.type = type;
            this.action = action;
            this.slotType = slotType;
            this.slot = slot;
            this.rawSlot = rawSlot;
            this.clickedItem = clickedItem;
            this.button = button;
            this.cursorItem = cursorItem;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && this.type == arg.type
                    && this.action == arg.action
                    && this.slotType == arg.slotType
                    && Objects.equals(this.slot, arg.slot)
                    && Objects.equals(this.rawSlot, arg.rawSlot)
                    && Objects.equals(this.clickedItem, arg.clickedItem)
                    && Objects.equals(this.button, arg.button)
                    && Objects.equals(this.cursorItem, arg.cursorItem);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                if (this.slot == null && this.rawSlot == null)
                    throw new IllegalArgumentException("slot and raw_slot cannot be null at the same time");

                if (this.button == null
                        && !(this.type == ClickType.LEFT
                        || this.type == ClickType.RIGHT
                        || this.type == ClickType.MIDDLE))
                    throw new IllegalArgumentException("button cannot be null when click type is not left, right or middle");

                ensureNotPresent(Argument.KEY_TARGET_PLAYER, this.getTargetSpecifier());
                ensurePresent(Argument.KEY_SLOT_TYPE, this.slotType);
                if (!(this.slot == null || this.rawSlot == null))
                    throw new IllegalArgumentException("cannot specify both slot and raw_slot in action execute scenario");
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_CLICK_TYPE, this.type,
                    KEY_INVENTORY_ACTION, this.action,
                    KEY_SLOT_TYPE, this.slotType,
                    KEY_SLOT, this.slot,
                    KEY_RAW_SLOT, this.rawSlot,
                    KEY_CLICKED_ITEM, this.clickedItem,
                    KEY_BUTTON, this.button,
                    KEY_CURSOR_ITEM, this.cursorItem
            );
        }
    }
}
