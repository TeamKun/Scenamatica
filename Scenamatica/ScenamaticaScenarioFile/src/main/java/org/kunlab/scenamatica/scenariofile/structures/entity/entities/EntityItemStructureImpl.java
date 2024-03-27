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
import org.kunlab.scenamatica.scenariofile.structures.inventory.ItemStackStructureImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityItemStructureImpl extends EntityStructureImpl implements EntityItemStructure
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

    public EntityItemStructureImpl(@NotNull EntityStructure original, @NotNull ItemStackStructure itemStack, @Nullable Integer pickupDelay,
                                   @Nullable UUID owner, @Nullable UUID thrower, Boolean canMobPickup, Boolean willAge)
    {
        super(EntityType.DROPPED_ITEM, original);
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
        Map<String, Object> map = EntityStructureImpl.serialize(structure, serializer);
        map.remove(KEY_TYPE);
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
        EntityStructureImpl.validate(map);

        if (map.containsKey(KEY_OWNER) && UUIDUtil.toUUIDOrNull((String) map.get(KEY_OWNER)) == null)
            throw new IllegalArgumentException("owner must be UUID");
        if (map.containsKey(KEY_THROWER) && UUIDUtil.toUUIDOrNull((String) map.get(KEY_THROWER)) == null)
            throw new IllegalArgumentException("thrower must be UUID");
    }

    @NotNull
    public static EntityItemStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
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

        Map<String, Object> copiedMap = new HashMap<>();
        // Entity と EntityItem で `type` がかぶる。
        copiedMap.putAll(map);
        if (copiedMap.containsKey(KEY_TYPE))
            copiedMap.put(KEY_TYPE, EntityType.DROPPED_ITEM.name());

        return new EntityItemStructureImpl(
                EntityStructureImpl.deserialize(copiedMap, serializer),
                itemStack,
                pickupDelay,
                ownerUUID,
                throwerUUID,
                MapUtils.getOrNull(copiedMap, KEY_CAN_MOB_PICKUP),
                MapUtils.getOrNull(copiedMap, KEY_WILL_AGE)
        );
    }

    @NotNull
    public static EntityItemStructure of(@NotNull Item entity)
    {
        return new EntityItemStructureImpl(
                EntityStructureImpl.of(entity),
                ItemStackStructureImpl.of(entity.getItemStack()),
                entity.getPickupDelay(),
                entity.getOwner(),
                entity.getThrower(),
                entity.canMobPickup(),
                willAge(entity)
        );
    }

    private static boolean willAge(Item entity)
    {
        return entity.getTicksLived() != Short.MIN_VALUE;
    }

    private static void setWillAge(Item entity, boolean willAge)
    {
        entity.setTicksLived(willAge ? 0: Short.MIN_VALUE);
    }

    @Override
    public void applyTo(Item entity)
    {
        super.applyToEntity(entity);

        if (this.pickupDelay != null)
            entity.setPickupDelay(this.pickupDelay);
        if (this.owner != null)
            entity.setOwner(this.owner);
        if (this.thrower != null)
            entity.setThrower(this.thrower);
        if (this.canMobPickup != null)
            entity.setCanMobPickup(this.canMobPickup);
        if (this.willAge != null)
            setWillAge(entity, this.willAge);
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof Item;
    }

    @Override
    public boolean isAdequate(Item object, boolean strict)
    {
        return super.isAdequateEntity(object, strict)
                && (this.pickupDelay == null || this.pickupDelay.equals(object.getPickupDelay()))
                && (this.owner == null || this.owner.equals(object.getOwner()))
                && (this.thrower == null || this.thrower.equals(object.getThrower()))
                && (this.canMobPickup == null || this.canMobPickup.equals(object.canMobPickup()))
                && (this.willAge == null || this.willAge.equals(willAge(object)))
                && this.itemStack.isAdequate(object.getItemStack(), strict);
    }
}
