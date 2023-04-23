package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.BeanUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.ItemStackBeanImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerHotbarSlotAction extends AbstractPlayerAction<PlayerHotbarSlotAction.PlayerHotbarSlotActionArgument>
{
    public static final String KEY_ACTION_NAME = "player_hotbar";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PlayerHotbarSlotActionArgument argument)
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
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable PlayerHotbarSlotActionArgument argument)
    {
        argument = super.requireArgsNonNull(argument);

        if (type != ScenarioType.ACTION_EXECUTE)
            return;

        if (argument.getPreviousSlot() != -1)
            throw new IllegalArgumentException("previous_slot cannot be specified in execute mode.");
    }

    @Override
    public boolean isFired(@NotNull PlayerHotbarSlotActionArgument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof PlayerItemHeldEvent;
        PlayerItemHeldEvent e = (PlayerItemHeldEvent) event;

        if (!super.isFired(argument, engine, event))
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
    public PlayerHotbarSlotActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, PlayerHotbarSlotActionArgument.KEY_CURRENT_SLOT, Integer.class);

        int currentSlot = (int) map.get(PlayerHotbarSlotActionArgument.KEY_CURRENT_SLOT);
        if (currentSlot < 0 || currentSlot > 8)
            throw new IllegalArgumentException("currentSlot must be between 0 and 8");

        int previousSlot = -1;
        if (map.containsKey(PlayerHotbarSlotActionArgument.KEY_PREVIOUS_SLOT))
        {
            MapUtils.checkType(map, PlayerHotbarSlotActionArgument.KEY_PREVIOUS_SLOT, Integer.class);
            previousSlot = (int) map.get(PlayerHotbarSlotActionArgument.KEY_PREVIOUS_SLOT);
            if (previousSlot < 0 || previousSlot > 8)
                throw new IllegalArgumentException("previousSlot must be between 0 and 8");
        }

        ItemStackBean item = null;
        if (map.containsKey(PlayerHotbarSlotActionArgument.KEY_CURRENT_ITEM))
        {
            Map<String, Object> itemMap = MapUtils.checkAndCastMap(
                    map.get(PlayerHotbarSlotActionArgument.KEY_CURRENT_ITEM),
                    String.class,
                    Object.class
            );

            ItemStackBeanImpl.validate(itemMap);

            item = ItemStackBeanImpl.deserialize(itemMap);
        }

        return new PlayerHotbarSlotActionArgument(
                super.deserializeTarget(map),
                currentSlot,
                previousSlot,
                item
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class PlayerHotbarSlotActionArgument extends AbstractPlayerActionArgument
    {
        public static final String KEY_CURRENT_SLOT = "slot";
        public static final String KEY_PREVIOUS_SLOT = "previous";
        public static final String KEY_CURRENT_ITEM = "item";

        int currentSlot;
        int previousSlot;
        @Nullable
        ItemStackBean currentItem;

        public PlayerHotbarSlotActionArgument(@NotNull String target, int currentSlot, int previousSlot, @Nullable ItemStackBean currentItem)
        {
            super(target);
            this.currentSlot = currentSlot;
            this.previousSlot = previousSlot;
            this.currentItem = currentItem;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            return super.isSame(argument);
        }

        @Override
        public String getArgumentString()
        {
            StringBuilder sb = new StringBuilder("slot=")
                    .append(this.currentSlot);

            if (this.previousSlot != -1)
                sb.append(", previous=").append(this.previousSlot);

            if (this.currentItem != null)
                sb.append(", item=").append(this.currentItem);

            return sb.toString();
        }
    }
}
