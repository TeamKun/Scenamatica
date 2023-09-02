package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerHotbarSlotAction extends AbstractPlayerAction<PlayerHotbarSlotAction.Argument>
        implements Executable<PlayerHotbarSlotAction.Argument>, Watchable<PlayerHotbarSlotAction.Argument>, Requireable<PlayerHotbarSlotAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_hotbar";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PlayerHotbarSlotAction.Argument argument)
    {
        argument = super.requireArgsNonNull(argument);

        Player p = argument.getTarget();
        int slot = argument.getCurrentSlot();
        ItemStackBean item = argument.getCurrentItem();

        p.getInventory().setHeldItemSlot(slot);
        if (item != null)
            p.getInventory().setItemInMainHand(item.toItemStack());
    }

    @Override
    public boolean isFired(@NotNull PlayerHotbarSlotAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof PlayerItemHeldEvent;
        PlayerItemHeldEvent e = (PlayerItemHeldEvent) event;

        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        int currentSlot = e.getNewSlot();
        int expectedCurrentSlot = argument.getCurrentSlot();
        int previousSlot = e.getPreviousSlot();
        int expectedPreviousSlot = argument.getPreviousSlot();
        ItemStack currentItem = e.getPlayer().getInventory().getItem(currentSlot);
        ItemStackBean expectedCurrentItem = argument.getCurrentItem();

        return currentSlot == expectedCurrentSlot
                && (expectedPreviousSlot == -1 || previousSlot == expectedPreviousSlot)
                && (expectedCurrentItem == null || BeanUtils.isSame(expectedCurrentItem, currentItem, false));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemHeldEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkType(map, Argument.KEY_CURRENT_SLOT, Integer.class);

        int currentSlot = (int) map.get(Argument.KEY_CURRENT_SLOT);
        if (currentSlot < 0 || currentSlot > 8)
            throw new IllegalArgumentException("currentSlot must be between 0 and 8");

        int previousSlot = -1;
        if (map.containsKey(Argument.KEY_PREVIOUS_SLOT))
        {
            MapUtils.checkType(map, Argument.KEY_PREVIOUS_SLOT, Integer.class);
            previousSlot = (int) map.get(Argument.KEY_PREVIOUS_SLOT);
            if (previousSlot < 0 || previousSlot > 8)
                throw new IllegalArgumentException("previousSlot must be between 0 and 8");
        }

        ItemStackBean item = null;
        if (map.containsKey(Argument.KEY_CURRENT_ITEM))
        {
            Map<String, Object> itemMap = MapUtils.checkAndCastMap(
                    map.get(Argument.KEY_CURRENT_ITEM),
                    String.class,
                    Object.class
            );

            serializer.validateItemStack(itemMap);

            item = serializer.deserializeItemStack(itemMap);
        }

        return new Argument(
                super.deserializeTarget(map),
                currentSlot,
                previousSlot,
                item
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PlayerHotbarSlotAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = super.requireArgsNonNull(argument);

        Player p = argument.getTarget();
        int currentSlot = argument.getCurrentSlot();
        ItemStackBean currentItem = argument.getCurrentItem();

        return p.getInventory().getHeldItemSlot() == currentSlot
                && (currentItem == null || BeanUtils.isSame(currentItem, p.getInventory().getItemInMainHand(), false));
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_CURRENT_SLOT = "slot";
        public static final String KEY_PREVIOUS_SLOT = "previous";
        public static final String KEY_CURRENT_ITEM = "item";

        int currentSlot; // TODO: Make this Nullable
        int previousSlot;
        @Nullable
        ItemStackBean currentItem;

        public Argument(String target, int currentSlot, int previousSlot, @Nullable ItemStackBean currentItem)
        {
            super(target);
            this.currentSlot = currentSlot;
            this.previousSlot = previousSlot;
            this.currentItem = currentItem;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && this.currentSlot == arg.currentSlot
                    && Objects.equals(this.currentItem, arg.currentItem);
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_CURRENT_SLOT, this.currentSlot,
                    KEY_PREVIOUS_SLOT, this.previousSlot,
                    KEY_CURRENT_ITEM, this.currentItem
            );
        }
    }
}
