package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityPickupItemAction extends AbstractEntityAction<EntityPickupItemAction.Argument>
        implements Executable<EntityPickupItemAction.Argument>, Watchable<EntityPickupItemAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_pickup_item";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Entity target = argument.selectTarget();
        if (!(target instanceof LivingEntity))
            throw new IllegalArgumentException("Target is not living entity.");

        LivingEntity leTarget = (LivingEntity) target;
        if (!leTarget.getCanPickupItems())
            throw new IllegalStateException("The target cannot pickup items (LivingEntity#getCanPickupItems() is false).");

        Item item;
        if (argument.isItemSelectable())
            item = argument.selectItem();
        else
        {
            EntityItemStructure itemStructure = argument.getItem();

            // 拾う前に, アイテムを落とす必要がある
            item = target.getWorld().dropItemNaturally(
                    target.getLocation(),
                    argument.getItem().getItemStack().create(),
                    itemStructure::applyTo
            );
        }

        boolean isPlayer = target instanceof Player;

        if (isPlayer && !item.canMobPickup())
            throw new IllegalStateException("The item cannot be picked up by mobs (Item#canMobPickup() is false).");
        else if (!isPlayer && !item.canPlayerPickup())
            throw new IllegalStateException("The item cannot be picked up by players (Item#canPlayerPickup() is false).");

        // NMS にすら アイテムを拾ったことを検知する API がないので偽造する
        EntityPickupItemEvent event = new EntityPickupItemEvent(leTarget, item, 0);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            throw new IllegalStateException("Item pickup event is cancelled.");

        int quantity = item.getItemStack().getAmount() - event.getRemaining();
        leTarget.playPickupItemAnimation(item, quantity);
        item.getItemStack().setAmount(event.getRemaining());

        if (isPlayer)
        {
            Player player = (Player) target;
            Inventory inventory = player.getInventory();
            inventory.addItem(item.getItemStack());
        }

        if (event.getRemaining() <= 0)
            item.remove();
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        EntityPickupItemEvent e = (EntityPickupItemEvent) event;

        return (argument.getRemaining() == null || Objects.equals(argument.getRemaining(), e.getRemaining()))
                && (argument.getItem() == null || argument.getItem().isAdequate(e.getItem()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityPickupItemEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        Integer remaining = MapUtils.getAsNumberOrNull(map, Argument.KEY_REMAINING, Number::intValue);

        if (!map.containsKey(Argument.KEY_ITEM))
            return new Argument(
                    super.deserializeTarget(map, serializer),
                    remaining,
                    (String) null
            );

        Object mayItem = map.get(Argument.KEY_ITEM);
        if (mayItem instanceof String)
            return new Argument(
                    super.deserializeTarget(map, serializer),
                    remaining,
                    (String) mayItem
            );
        else
            return new Argument(
                    super.deserializeTarget(map, serializer),
                    remaining,
                    serializer.deserialize(
                            MapUtils.checkAndCastMap(map.get(Argument.KEY_ITEM)),
                            EntityItemStructure.class
                    )
            );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument<Entity>
    {
        public static final String KEY_REMAINING = "remaining";
        public static final String KEY_ITEM = "item";

        Integer remaining;
        EntityItemStructure item;
        String itemSelector;

        public Argument(@Nullable EntityArgumentHolder<Entity> mayTarget, Integer remaining, EntityItemStructure item)
        {
            super(mayTarget);
            this.remaining = remaining;
            this.item = item;
            this.itemSelector = null;
        }

        public Argument(@Nullable EntityArgumentHolder<Entity> mayTarget, Integer remaining, String itemSelector)
        {
            super(mayTarget);
            this.remaining = remaining;
            this.item = null;
            this.itemSelector = itemSelector;
        }

        public boolean isItemSelectable()
        {
            return this.itemSelector != null;
        }

        public Item selectItem()
        {
            if (!this.isItemSelectable())
                throw new IllegalArgumentException("Item is not selectable.");

            List<Entity> entities = EntityUtils.selectEntities(this.itemSelector);
            if (entities.isEmpty())
                throw new IllegalStateException("No entity found: " + this.itemSelector);
            else if (entities.size() > 1)
                throw new IllegalStateException("Multiple entities found: " + this.itemSelector);

            Entity entity = entities.get(0);
            if (!(entity instanceof Item))
                throw new IllegalStateException("Selected entity is not an item: " + this.itemSelector);

            return (Item) entity;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.item, arg.item);
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
