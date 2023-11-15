package org.kunlab.scenamatica.scenariofile.beans.entity.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.UUIDUtil;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.scenariofile.beans.entity.EntityBeanImpl;

import java.util.Map;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityItemBeanImpl extends EntityBeanImpl implements EntityItemBean
{
    @NotNull
    ItemStackBean itemStack;

    @Nullable
    Integer pickupDelay;
    @Nullable
    UUID owner;

    @Nullable
    UUID thrower;

    Boolean canMobPickup;

    Boolean willAge;

    public EntityItemBeanImpl(@NotNull EntityBean bean, @NotNull ItemStackBean itemStack, @Nullable Integer pickupDelay,
                              @Nullable UUID owner, @Nullable UUID thrower, Boolean canMobPickup, Boolean willAge)
    {
        super(
                EntityType.DROPPED_ITEM,
                bean.getLocation(), bean.getVelocity(), bean.getCustomName(), bean.getUuid(),
                bean.getGlowing(), bean.getGravity(), bean.getSilent(), bean.getCustomNameVisible(),
                bean.getInvulnerable(), bean.getTags(), bean.getMaxHealth(), bean.getHealth(),
                bean.getLastDamageCause(), bean.getPotionEffects(), bean.getFireTicks(), bean.getTicksLived(),
                bean.getPortalCooldown(), bean.getPersistent(), bean.getFallDistance()
        );
        this.itemStack = itemStack;
        this.pickupDelay = pickupDelay;
        this.owner = owner;
        this.thrower = thrower;
        this.canMobPickup = canMobPickup;
        this.willAge = willAge;
    }

    public static Map<String, Object> serialize(@NotNull EntityItemBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = serializer.serializeEntity(bean);
        map.remove(EntityBean.KEY_TYPE);
        map.putAll(serializer.serializeItemStack(bean.getItemStack()));

        if (bean.getPickupDelay() != null)
            map.put(KEY_PICKUP_DELAY, bean.getPickupDelay());
        if (bean.getOwner() != null)
            map.put(KEY_OWNER, bean.getOwner().toString());
        if (bean.getThrower() != null)
            map.put(KEY_THROWER, bean.getThrower().toString());
        if (bean.getCanMobPickup() != null)
            map.put(KEY_CAN_MOB_PICKUP, bean.getCanMobPickup());
        if (bean.getWillAge() != null)
            map.put(KEY_WILL_AGE, bean.getWillAge());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        serializer.validateEntity(map);

        if (map.containsKey(KEY_OWNER) && UUIDUtil.toUUIDOrNull((String) map.get(KEY_OWNER)) == null)
            throw new IllegalArgumentException("owner must be UUID");
        if (map.containsKey(KEY_THROWER) && UUIDUtil.toUUIDOrNull((String) map.get(KEY_THROWER)) == null)
            throw new IllegalArgumentException("thrower must be UUID");
    }

    public static EntityItemBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        serializer.validateEntity(map);
        validate(map, serializer);

        Number pickupDelayNum = MapUtils.getAsNumberOrNull(map, KEY_PICKUP_DELAY);
        Integer pickupDelay = pickupDelayNum != null ? pickupDelayNum.intValue(): null;

        UUID ownerUUID = null;
        if (map.containsKey(KEY_OWNER))
            ownerUUID = UUIDUtil.toUUIDOrThrow((String) map.get(KEY_OWNER));

        UUID throwerUUID = null;
        if (map.containsKey(KEY_THROWER))
            throwerUUID = UUIDUtil.toUUIDOrThrow((String) map.get(KEY_THROWER));

        ItemStackBean itemStack = serializer.deserializeItemStack(map);

        // Entity と EntityItem で `type` がかぶる。
        if (map.containsKey(KEY_TYPE))
            map.put(KEY_TYPE, EntityType.DROPPED_ITEM.name());

        return new EntityItemBeanImpl(
                serializer.deserializeEntity(map),
                itemStack,
                pickupDelay,
                ownerUUID,
                throwerUUID,
                MapUtils.getOrNull(map, KEY_CAN_MOB_PICKUP),
                MapUtils.getOrNull(map, KEY_WILL_AGE)
        );
    }
}
