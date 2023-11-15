package org.kunlab.scenamatica.action.actions.inventory.interact;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCreativeEvent;
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
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryCreativeAction extends InventoryClickAction<InventoryCreativeAction.Argument>
        implements Executable<InventoryCreativeAction.Argument>, Watchable<InventoryCreativeAction.Argument>
{
    public static final String KEY_ACTION_NAME = "inventory_creative";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        int slot = argument.getSlot();
        Actor actor = PlayerUtils.getActorOrThrow(engine, argument.getTarget());

        actor.giveCreativeItem(slot, argument.getItem().toItemStack());
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        InventoryCreativeEvent e = (InventoryCreativeEvent) event;

        return super.isFired(argument, engine, event)
                && (argument.getItem() == null || BeanUtils.isSame(argument.getItem(), e.getCursor(), false));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryCreativeEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        ItemStackBean stack = null;
        if (map.containsKey(Argument.KEY_ITEM))
            stack = serializer.deserialize(
                    MapUtils.checkAndCastMap(Argument.KEY_ITEM),
                    ItemStackBean.class
            );

        return new Argument(
                super.deserializeArgument(map, serializer),
                stack
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends InventoryClickAction.Argument
    {
        public static final String KEY_ITEM = "item";

        ItemStackBean item;

        public Argument(InventoryClickAction.Argument argument, ItemStackBean item)
        {
            super(
                    argument.getInventory(),
                    argument.getTargetSpecifier(),
                    ClickType.CREATIVE,
                    InventoryAction.PLACE_ALL,
                    argument.getSlotType(),
                    argument.getSlot(),
                    argument.getRawSlot(),
                    argument.getClickedItem(),
                    argument.getButton(),
                    argument.getCursorItem()
            );
            this.item = item;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensurePresent(KEY_ITEM, this.item);
                ensurePresent(KEY_SLOT, this.getSlot());
            }
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg) &&
                    Objects.equals(this.item, arg.item);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ITEM, this.item
            );
        }
    }
}
