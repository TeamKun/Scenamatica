package org.kunlab.scenamatica.structures.minecraft.entity.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.ItemStackStructureImpl;
import org.kunlab.scenamatica.structures.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.structures.specifiers.PlayerSpecifierImpl;

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

    public static void validateItem(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        EntityStructureImpl.validate(node);
    }

    @NotNull
    public static EntityItemStructure deserializeItem(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validateItem(node, serializer);

        Integer pickupDelay = node.get(KEY_PICKUP_DELAY).asInteger(null);

        EntitySpecifier<?> owner;
        if (node.containsKey(KEY_OWNER))
            owner = serializer.tryDeserializeEntitySpecifier(node.get(KEY_OWNER));
        else
            owner = EntitySpecifierImpl.EMPTY;

        EntitySpecifier<?> thrower;
        if (node.containsKey(KEY_THROWER))
            thrower = serializer.tryDeserializeEntitySpecifier(node.get(KEY_THROWER));
        else
            thrower = EntitySpecifierImpl.EMPTY;

        ItemStackStructure itemStack = serializer.deserialize(node, ItemStackStructure.class);

        Boolean canMobPickup = node.get(KEY_CAN_MOB_PICKUP).asBoolean(null);
        Boolean willAge = node.get(KEY_WILL_AGE).asBoolean(null);

        return new EntityItemStructureImpl(
                EntityStructureImpl.deserialize(node, serializer, EntityType.DROPPED_ITEM),
                itemStack,
                pickupDelay,
                owner,
                thrower,
                canMobPickup,
                willAge
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

    private static void setUUIDByEntitySpecifier(@NotNull EntitySpecifier<?> specifier, @NotNull Consumer<UUID> setter)
    {
        if (specifier.hasUUID())
            setter.accept(specifier.getSelectingUUID());
        else
            specifier.selectTarget(null)
                    .map(Entity::getUniqueId)
                    .ifPresent(setter);
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

    @Override
    public void applyTo(@NotNull Entity entity)
    {
        super.applyTo(entity);
        if (!(entity instanceof Item))
            return;
        Item itemEntity = (Item) entity;

        if (this.pickupDelay != null)
            itemEntity.setPickupDelay(this.pickupDelay);
        if (this.owner.canProvideTarget())
            setUUIDByEntitySpecifier(this.owner, itemEntity::setOwner);
        if (this.thrower.canProvideTarget())
            setUUIDByEntitySpecifier(this.thrower, itemEntity::setThrower);
        if (this.canMobPickup != null)
            itemEntity.setCanMobPickup(this.canMobPickup);
        if (this.willAge != null)
            setWillAge(itemEntity, this.willAge);
    }

    @Override
    public boolean isAdequate(@Nullable Entity entity, boolean strict)
    {
        if (!(super.isAdequate(entity, strict) && entity instanceof Item))
            return false;
        Item itemEntity = (Item) entity;

        return (this.pickupDelay == null || this.pickupDelay.equals(itemEntity.getPickupDelay()))
                && checkMatchedEntityByUUID(this.owner, itemEntity.getOwner())
                && checkMatchedEntityByUUID(this.thrower, itemEntity.getThrower())
                && (this.canMobPickup == null || this.canMobPickup.equals(itemEntity.canMobPickup()))
                && (this.willAge == null || this.willAge.equals(willAge(itemEntity)))
                && this.itemStack.isAdequate(itemEntity.getItemStack(), strict);
    }
}
