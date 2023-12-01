package org.kunlab.scenamatica.scenariofile.structures.entity.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.UUIDUtil;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.scenariofile.structures.entity.EntityStructureImpl;

import java.util.Map;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityItemStructureImpl extends EntityStructureImpl<Item> implements EntityItemStructure
{
    @NotNull
    ItemStackStructure itemStack;

    @Nullable
    Integer pickupDelay;
    @Nullable
    UUID owner;

    @Nullable
    UUID thrower;

    Boolean canMobPickup;

    Boolean willAge;

    public EntityItemStructureImpl(@NotNull EntityStructure<?> structure, @NotNull ItemStackStructure itemStack, @Nullable Integer pickupDelay,
                                   @Nullable UUID owner, @Nullable UUID thrower, Boolean canMobPickup, Boolean willAge)
    {
        super(  // TODO: Extended super argument
                EntityType.DROPPED_ITEM,
                structure.getLocation(), structure.getVelocity(), structure.getCustomName(), structure.getUuid(),
                structure.getGlowing(), structure.getGravity(), structure.getSilent(), structure.getCustomNameVisible(),
                structure.getInvulnerable(), structure.getTags(), structure.getMaxHealth(), structure.getHealth(),
                structure.getLastDamageCause(), structure.getPotionEffects(), structure.getFireTicks(), structure.getTicksLived(),
                structure.getPortalCooldown(), structure.getPersistent(), structure.getFallDistance()
        );
        this.itemStack = itemStack;
        this.pickupDelay = pickupDelay;
        this.owner = owner;
        this.thrower = thrower;
        this.canMobPickup = canMobPickup;
        this.willAge = willAge;
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull EntityItemStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = serializer.serialize(structure, EntityStructure.class);
        map.remove(EntityStructure.KEY_TYPE);
        map.putAll(serializer.serialize(structure.getItemStack(), ItemStackStructure.class));

        if (structure.getPickupDelay() != null)
            map.put(KEY_PICKUP_DELAY, structure.getPickupDelay());
        if (structure.getOwner() != null)
            map.put(KEY_OWNER, structure.getOwner().toString());
        if (structure.getThrower() != null)
            map.put(KEY_THROWER, structure.getThrower().toString());
        if (structure.getCanMobPickup() != null)
            map.put(KEY_CAN_MOB_PICKUP, structure.getCanMobPickup());
        if (structure.getWillAge() != null)
            map.put(KEY_WILL_AGE, structure.getWillAge());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        serializer.validate(map, EntityStructure.class);

        if (map.containsKey(KEY_OWNER) && UUIDUtil.toUUIDOrNull((String) map.get(KEY_OWNER)) == null)
            throw new IllegalArgumentException("owner must be UUID");
        if (map.containsKey(KEY_THROWER) && UUIDUtil.toUUIDOrNull((String) map.get(KEY_THROWER)) == null)
            throw new IllegalArgumentException("thrower must be UUID");
    }

    @NotNull
    public static EntityItemStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        serializer.validate(map, EntityStructure.class);
        validate(map, serializer);

        Number pickupDelayNum = MapUtils.getAsNumberOrNull(map, KEY_PICKUP_DELAY);
        Integer pickupDelay = pickupDelayNum != null ? pickupDelayNum.intValue(): null;

        UUID ownerUUID = null;
        if (map.containsKey(KEY_OWNER))
            ownerUUID = UUIDUtil.toUUIDOrThrow((String) map.get(KEY_OWNER));

        UUID throwerUUID = null;
        if (map.containsKey(KEY_THROWER))
            throwerUUID = UUIDUtil.toUUIDOrThrow((String) map.get(KEY_THROWER));

        ItemStackStructure itemStack = serializer.deserialize(map, ItemStackStructure.class);

        // Entity と EntityItem で `type` がかぶる。
        if (map.containsKey(KEY_TYPE))
            map.put(KEY_TYPE, EntityType.DROPPED_ITEM.name());

        return new EntityItemStructureImpl(
                serializer.deserialize(map, EntityStructure.class),
                itemStack,
                pickupDelay,
                ownerUUID,
                throwerUUID,
                MapUtils.getOrNull(map, KEY_CAN_MOB_PICKUP),
                MapUtils.getOrNull(map, KEY_WILL_AGE)
        );
    }

    @Override
    public Item create()
    {
        return null;
    }

    @Override
    public void applyTo(Item entity)
    {
        super.applyTo(entity);

        if (this.pickupDelay != null)
            entity.setPickupDelay(this.pickupDelay);
        if (this.owner != null)
            entity.setOwner(this.owner);
        if (this.thrower != null)
            entity.setThrower(this.thrower);
        if (this.canMobPickup != null)
            entity.setCanMobPickup(this.canMobPickup);
        if (this.willAge != null)
            entity.setWillAge(this.willAge);
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return super.canApplyTo(target) && target instanceof Item;
    }

    @Override
    public boolean isAdequate(Item object, boolean strict)
    {
        return false;
    }
}
