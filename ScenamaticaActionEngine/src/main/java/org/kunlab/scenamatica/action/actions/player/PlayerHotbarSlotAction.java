package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
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
    public void execute(@NotNull ScenarioEngine engine, @NotNull PlayerHotbarSlotAction.Argument argument)
    {
        Player p = argument.getTarget(engine);
        int slot = argument.getCurrentSlot();
        ItemStackStructure item = argument.getCurrentItem();

        p.getInventory().setHeldItemSlot(slot);
        if (item != null)
            p.getInventory().setItemInMainHand(item.create());
    }

    @Override
    public boolean isFired(@NotNull PlayerHotbarSlotAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerItemHeldEvent;
        PlayerItemHeldEvent e = (PlayerItemHeldEvent) event;

        int currentSlot = e.getNewSlot();
        Integer expectedCurrentSlot = argument.getCurrentSlot();
        int previousSlot = e.getPreviousSlot();
        Integer expectedPreviousSlot = argument.getPreviousSlot();
        ItemStack currentItem = e.getPlayer().getInventory().getItem(currentSlot);
        ItemStackStructure expectedCurrentItem = argument.getCurrentItem();

        return (expectedCurrentSlot == null || currentSlot == expectedCurrentSlot)
                && (expectedPreviousSlot == null || previousSlot == expectedPreviousSlot)
                && (expectedCurrentItem == null || expectedCurrentItem.isAdequate(currentItem));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemHeldEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        Integer currentSlot = MapUtils.getAsNumberOrNull(map, Argument.KEY_CURRENT_SLOT, Number::intValue);
        Integer previousSlot = MapUtils.getAsNumberOrNull(map, Argument.KEY_PREVIOUS_SLOT, Number::intValue);

        ItemStackStructure item = null;
        if (map.containsKey(Argument.KEY_CURRENT_ITEM))
        {
            Map<String, Object> itemMap =
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_CURRENT_ITEM));

            serializer.validate(itemMap, ItemStackStructure.class);

            item = serializer.deserialize(itemMap, ItemStackStructure.class);
        }

        return new Argument(
                super.deserializeTarget(map, serializer),
                currentSlot,
                previousSlot,
                item
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull PlayerHotbarSlotAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        Player p = argument.getTarget(engine);
        Integer currentSlot = argument.getCurrentSlot();
        ItemStackStructure currentItem = argument.getCurrentItem();

        return (currentSlot == null || p.getInventory().getHeldItemSlot() == currentSlot)
                && (currentItem == null || currentItem.isAdequate(p.getInventory().getItemInMainHand()));
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_CURRENT_SLOT = "slot";
        public static final String KEY_PREVIOUS_SLOT = "previous";
        public static final String KEY_CURRENT_ITEM = "item";

        Integer currentSlot;
        Integer previousSlot;
        ItemStackStructure currentItem;

        public Argument(PlayerSpecifier target, Integer currentSlot, Integer previousSlot, @Nullable ItemStackStructure currentItem)
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
                    && Objects.equals(this.currentSlot, arg.currentSlot)
                    && Objects.equals(this.currentItem, arg.currentItem);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            switch (type)
            {
                case ACTION_EXECUTE:
                    ensurePresent(KEY_CURRENT_SLOT, this.currentSlot);
                    ensureNotPresent(KEY_CURRENT_ITEM, this.currentItem);
                    /* fall through */
                case CONDITION_REQUIRE:
                    ensureNotPresent(KEY_PREVIOUS_SLOT, this.previousSlot);
                    break;
            }

            if (this.currentSlot != null && (this.currentSlot < 0 || this.currentSlot > 8))
                throw new IllegalArgumentException("Slot must be between 0 and 8");
            if (this.previousSlot != null && (this.previousSlot < 0 || this.previousSlot > 8))
                throw new IllegalArgumentException("Previous slot must be between 0 and 8");
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_CURRENT_SLOT, this.currentSlot,
                    KEY_PREVIOUS_SLOT, this.previousSlot,
                    KEY_CURRENT_ITEM, this.currentItem
            );
        }
    }
}
