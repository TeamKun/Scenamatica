package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityItemBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityDropItemAction extends AbstractEntityAction<EntityDropItemAction.Argument>
        implements Executable<EntityDropItemAction.Argument>, Watchable<EntityDropItemAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_drop_item";

    private static void applyBean(EntityItemBean bean, Entity dropper, Item entity)
    {
        EntityDropItemEvent event = new EntityDropItemEvent(dropper, entity);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
        {
            entity.remove();
            return;
        }

        if (bean.getPickupDelay() != null)
            entity.setPickupDelay(bean.getPickupDelay());
        if (bean.getOwner() != null)
            entity.setOwner(bean.getOwner());
        if (bean.getThrower() != null)
            entity.setThrower(bean.getThrower());
        if (bean.getVelocity() != null)
            entity.setVelocity(bean.getVelocity());
        if (bean.getCanMobPickup() != null)
            entity.setCanMobPickup(bean.getCanMobPickup());
        if (bean.getWillAge() != null)
            entity.setWillAge(bean.getWillAge());
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        assert argument != null;
        Entity target = argument.selectTarget();
        if (!(target instanceof InventoryHolder))
            throw new IllegalArgumentException("Target is not inventory holder.");

        target.getWorld().dropItemNaturally(
                target.getLocation(),
                argument.getItem().getItemStack().toItemStack(),
                (entity) -> applyBean(argument.getItem(), target, entity)
        );
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        EntityDropItemEvent e = (EntityDropItemEvent) event;
        Item item = e.getItemDrop();
        EntityItemBean bean = argument.getItem();

        return bean == null
                || (bean.getPickupDelay() == null || Objects.equals(bean.getPickupDelay(), item.getPickupDelay())
                && (bean.getOwner() == null || Objects.equals(bean.getOwner(), item.getOwner()))
                && (bean.getThrower() == null || Objects.equals(bean.getThrower(), item.getThrower()))
                && (bean.getVelocity() == null || Utils.vectorEquals(bean.getVelocity(), item.getVelocity()))
                && (bean.getCanMobPickup() == null || Objects.equals(bean.getCanMobPickup(), item.canMobPickup()))
                && (bean.getWillAge() == null || Objects.equals(bean.getWillAge(), item.willAge())
                && BeanUtils.isSame(bean.getItemStack(), item.getItemStack(), false))
        );
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDropItemEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        EntityItemBean item = null;
        if (map.containsKey(Argument.KEY_DROP_ITEM))
            item = serializer.deserializeEntityItem(
                    MapUtils.checkAndCastMap(
                            map.get(Argument.KEY_DROP_ITEM),
                            String.class,
                            Object.class
                    ));

        return new Argument(
                super.deserializeTarget(map, serializer),
                item
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument
    {
        public static final String KEY_DROP_ITEM = "item";

        EntityItemBean item;

        public Argument(@Nullable Object mayTarget, EntityItemBean item)
        {
            super(mayTarget);
            this.item = item;
        }

        public Argument(@NotNull String target, EntityItemBean item)
        {
            super(target);
            this.item = item;
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
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                if (!this.isSelectable())
                    throw new IllegalArgumentException("Cannot select target for this action, please specify target with valid selector.");

                throwIfNotPresent(KEY_DROP_ITEM, this.item);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_DROP_ITEM, this.item
            );
        }
    }
}
