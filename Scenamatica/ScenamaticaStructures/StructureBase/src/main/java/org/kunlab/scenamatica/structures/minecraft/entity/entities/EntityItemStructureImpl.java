package org.kunlab.scenamatica.structures.minecraft.entity.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.UUIDUtil;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.ItemStackStructureImpl;
import org.kunlab.scenamatica.structures.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.structures.specifiers.PlayerSpecifierImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Value
@EqualsAndHashCode(callSuper = true)
public class EntityItemStructureImpl extends EntityStructureImpl implements EntityItemStructure
{
    @NotNull
    ItemStackStructure itemStack;

    Integer pickupDelay;

    @NotNull
    EntitySpecifier<?> owner;
    @NotNull
    EntitySpecifier<?> thrower;

    Boolean canMobPickup;
    Boolean willAge;

    public EntityItemStructureImpl(@NotNull EntityStructure original, @NotNull ItemStackStructure itemStack, @Nullable Integer pickupDelay,
                                   @NotNull EntitySpecifier<?> owner, @NotNull EntitySpecifier<?> thrower, Boolean canMobPickup, Boolean willAge)
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
    public static Map<String, Object> serializeItem(@NotNull EntityItemStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = EntityStructureImpl.serialize(structure, serializer);
        map.remove(KEY_TYPE);
        map.putAll(serializer.serialize(structure.getItemStack(), ItemStackStructure.class));

        if (structure.getPickupDelay() != null)
            map.put(KEY_PICKUP_DELAY, structure.getPickupDelay());
        if (structure.getOwner().canProvideTarget())
            map.put(KEY_OWNER, structure.getOwner().getTargetRaw());
        if (structure.getThrower().canProvideTarget())
            map.put(KEY_THROWER, structure.getThrower().getTargetRaw());
        if (structure.getCanMobPickup() != null)
            map.put(KEY_CAN_MOB_PICKUP, structure.getCanMobPickup());
        if (structure.getWillAge() != null)
            map.put(KEY_WILL_AGE, structure.getWillAge());

        return map;
    }

    public static void validateItem(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        EntityStructureImpl.validate(map);
    }

    @NotNull
    public static EntityItemStructure deserializeItem(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validateItem(map, serializer);

        Number pickupDelayNum = MapUtils.getAsNumberOrNull(map, KEY_PICKUP_DELAY);
        Integer pickupDelay = pickupDelayNum != null ? pickupDelayNum.intValue(): null;

        EntitySpecifier<?> owner;
        if (map.containsKey(KEY_OWNER))
            owner = serializer.tryDeserializeEntitySpecifier(map.get(KEY_OWNER));
        else
            owner = EntitySpecifierImpl.EMPTY;

        EntitySpecifier<?> thrower;
        if (map.containsKey(KEY_THROWER))
            thrower = serializer.tryDeserializeEntitySpecifier(map.get(KEY_THROWER));
        else
            thrower = EntitySpecifierImpl.EMPTY;

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
                owner,
                thrower,
                MapUtils.getOrNull(copiedMap, KEY_CAN_MOB_PICKUP),
                MapUtils.getOrNull(copiedMap, KEY_WILL_AGE)
        );
    }

    @NotNull
    public static EntityItemStructure ofItem(@NotNull Item entity)
    {
        return new EntityItemStructureImpl(
                EntityStructureImpl.of(entity),
                ItemStackStructureImpl.of(entity.getItemStack()),
                entity.getPickupDelay(),
                PlayerSpecifierImpl.ofPlayer(entity.getOwner()),
                PlayerSpecifierImpl.ofPlayer(entity.getThrower()),
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
        super.applyTo(entity, true);

        if (this.pickupDelay != null)
            entity.setPickupDelay(this.pickupDelay);
        if (this.owner.canProvideTarget())
            setUUIDByEntitySpecifier(this.owner, entity::setOwner);
        if (this.thrower.canProvideTarget())
            setUUIDByEntitySpecifier(this.thrower, entity::setThrower);
        if (this.canMobPickup != null)
            entity.setCanMobPickup(this.canMobPickup);
        if (this.willAge != null)
            setWillAge(entity, this.willAge);
    }

    private static void setUUIDByEntitySpecifier(@NotNull EntitySpecifier<?> specifier, @NotNull Consumer<UUID> setter)
    {
        if (specifier.hasUUID())
            setter.accept(specifier.getSelectingUUID());
        else
            specifier.selectTarget(null)
                    .map(Entity::getUniqueId)
                    .ifPresent(setter);
    }

    @Override
    public boolean isAdequate(Item object, boolean strict)
    {
        return super.isAdequate(object, strict)
                && (this.pickupDelay == null || this.pickupDelay.equals(object.getPickupDelay()))
                && checkMatchedEntityByUUID(this.owner, object.getOwner())
                && checkMatchedEntityByUUID(this.thrower, object.getThrower())
                && (this.canMobPickup == null || this.canMobPickup.equals(object.canMobPickup()))
                && (this.willAge == null || this.willAge.equals(willAge(object)))
                && this.itemStack.isAdequate(object.getItemStack(), strict);
    }

    private static boolean checkMatchedEntityByUUID(@NotNull EntitySpecifier<?> entity, UUID uuid)
    {
        if (!entity.isSelectable())
            return true;

        if (entity.hasUUID())
            return entity.getSelectingUUID().equals(uuid);

        return entity.selectTarget(null)
                .map(Entity::getUniqueId)
                .map(uuid::equals)
                .orElse(false);
    }
}
