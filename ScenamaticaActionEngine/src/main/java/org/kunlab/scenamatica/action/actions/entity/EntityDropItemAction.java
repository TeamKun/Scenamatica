package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityDropItemAction extends AbstractEntityAction<EntityDropItemAction.Argument>
        implements Executable<EntityDropItemAction.Argument>, Watchable<EntityDropItemAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_drop_item";

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
                (entity) -> BeanUtils.applyItemBeanData(argument.getItem(), target, entity)
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
            item = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_DROP_ITEM)),
                    EntityItemBean.class
            );

        return new Argument(
                super.deserializeTarget(map, serializer),
                item
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument<Entity>
    {
        public static final String KEY_DROP_ITEM = "item";

        EntityItemBean item;

        public Argument(@Nullable EntityArgumentHolder<Entity> mayTarget, EntityItemBean item)
        {
            super(mayTarget);
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
                this.throwIfNotSelectable();
                ensurePresent(KEY_DROP_ITEM, this.item);
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
