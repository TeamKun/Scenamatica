package org.kunlab.scenamatica.structures.minecraft;

import lombok.Value;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WitherSkull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.PlayerStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.EntityItemStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.ProjectileStructureImpl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

class SelectiveEntityStructureSerializerMock
{
    private static final EnumMap<EntityType, EntityStructureEntry<?, ?>> ENTITY_STRUCTURES;

    static
    {
        ENTITY_STRUCTURES = new EnumMap<>(EntityType.class);

        registerEntities();
    }


    private static void registerEntities()
    {
        registerStructure(
                EntityType.DROPPED_ITEM,
                EntityItemStructure.class,
                Item.class,
                EntityItemStructureImpl::serializeItem,
                EntityItemStructureImpl::deserializeItem,
                EntityItemStructureImpl::validateItem,
                (entity, ignored) -> EntityItemStructureImpl.ofItem(entity)
        );/*
        registerStructure(
                EntityType.PLAYER,
                HumanEntityStructure.class,
                HumanEntityStructureImpl::serialize,
                HumanEntityStructureImpl::deserialize,
                HumanEntityStructureImpl::validate
        );*/

        registerStructure(
                EntityType.PLAYER,
                PlayerStructure.class,
                Player.class,
                PlayerStructureImpl::serializePlayer,
                PlayerStructureImpl::deserializePlayer,
                PlayerStructureImpl::validatePlayer,
                (player, ignored) -> PlayerStructureImpl.ofPlayer(player)
        );

        registerStructure(
                EntityType.UNKNOWN,
                EntityStructure.class,
                Entity.class,
                EntityStructureImpl::serialize,
                EntityStructureImpl::deserialize,
                (stringObjectMap, structureSerializer) -> EntityStructureImpl.validate(stringObjectMap),
                (entity, ignored) -> EntityStructureImpl.of(entity)
        );

        registerProjectiles();
    }

    private static void registerProjectiles()
    {
        BiFunction<ProjectileStructure, StructureSerializer, Map<String, Object>> serializer = ProjectileStructureImpl::serialize;
        BiFunction<Map<String, Object>, StructureSerializer, ProjectileStructure> deserializer = ProjectileStructureImpl::deserialize;
        BiConsumer<Map<String, Object>, StructureSerializer> validator = ProjectileStructureImpl::validate;
        BiFunction<Projectile, StructureSerializer, ProjectileStructure> constructor = ProjectileStructureImpl::ofSource;

        Map<EntityType, Class<? extends Projectile>> projectileTypes = new HashMap<EntityType, Class<? extends Projectile>>()
        {{
            this.put(EntityType.ARROW, Arrow.class);
            this.put(EntityType.DRAGON_FIREBALL, DragonFireball.class);
            this.put(EntityType.EGG, Egg.class);
            this.put(EntityType.ENDER_PEARL, EnderPearl.class);
            this.put(EntityType.SMALL_FIREBALL, SmallFireball.class);
            this.put(EntityType.FISHING_HOOK, FishHook.class);
            this.put(EntityType.FIREBALL, Fireball.class);
            this.put(EntityType.LLAMA_SPIT, LlamaSpit.class);
            this.put(EntityType.SHULKER_BULLET, ShulkerBullet.class);
            this.put(EntityType.SNOWBALL, Snowball.class);
            this.put(EntityType.SPECTRAL_ARROW, SpectralArrow.class);
            this.put(EntityType.THROWN_EXP_BOTTLE, ThrownExpBottle.class);
            this.put(EntityType.SPLASH_POTION, SplashPotion.class);
            this.put(EntityType.TRIDENT, Trident.class);
            this.put(EntityType.WITHER_SKULL, WitherSkull.class);
        }};

        for (Map.Entry<EntityType, Class<? extends Projectile>> projectileEntry : projectileTypes.entrySet())
        {
            registerStructure(
                    projectileEntry.getKey(),
                    ProjectileStructure.class,
                    Projectile.class,
                    serializer,
                    deserializer,
                    validator,
                    constructor
            );
        }
    }

    private static <E extends Entity, S extends EntityStructure & Mapped> void registerStructure(@NotNull EntityType entityType,
                                                                                                 @NotNull Class<S> clazz,
                                                                                                 @NotNull Class<? extends E> entityClazz,
                                                                                                 @NotNull BiFunction<S, StructureSerializer, Map<String, Object>> serializer,
                                                                                                 @NotNull BiFunction<Map<String, Object>, StructureSerializer, S> deserializer,
                                                                                                 @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator,
                                                                                                 @NotNull BiFunction<E, StructureSerializer, S> constructor)
    {
        if (entityType.getEntityClass() != entityClazz)
            throw new IllegalArgumentException("Entity class mismatch: " + entityType + " -?-> " + entityClazz);

        ENTITY_STRUCTURES.put(
                entityType,
                new EntityStructureEntry<>(
                        clazz,
                        serializer,
                        deserializer,
                        validator,
                        constructor
                )
        );
    }

    public static <T extends EntityStructure & Mapped> T deserialize(@NotNull EntityType entityType,
                                                                     @NotNull Map<String, Object> data,
                                                                     @NotNull StructureSerializer serializer)
    {
        // noinspection unchecked
        EntityStructureEntry<?, T> entry = (EntityStructureEntry<?, T>) ENTITY_STRUCTURES.get(entityType);
        if (entry == null)
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        return entry.getDeserializer().apply(data, serializer);
    }

    public static <T extends EntityStructure> T deserialize(@NotNull Map<String, Object> data,
                                                            @NotNull StructureSerializer serializer,
                                                            @NotNull Class<T> clazz)
    {
        EntityType type = getEntityTypeSafe(clazz);
        if (type == EntityType.UNKNOWN)
            type = guessByMapValue(data, type);

        boolean canGeneralize = clazz == EntityStructure.class;
        EntityStructureEntry<?, ?> entry = ENTITY_STRUCTURES.get(type);
        if (entry == null)
            if (canGeneralize)
                // noinspection unchecked  canGeneralize が true なら, clazz は EntityStructure.class である。
                return (T) EntityStructureImpl.deserialize(data, serializer);
            else
                throw new IllegalArgumentException("Unknown entity type: " + type);

        return (T) entry.getDeserializer().apply(data, serializer);
    }

    public static <T extends EntityStructure> T deserialize(@NotNull Class<T> clazz,
                                                            @NotNull Map<String, Object> data,
                                                            @NotNull StructureSerializer serializer)
    {
        // noinspection unchecked
        return deserialize(getEntityTypeSafe(clazz), data, serializer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends EntityStructure> Map<String, Object> serialize(@NotNull T entityStructure,
                                                                            @NotNull StructureSerializer serializer,
                                                                            @Nullable Class<? extends T> clazz)
    {
        EntityType type = entityStructure.getType();
        if (type == null && clazz != null)
            type = getEntityTypeSafe(clazz);

        EntityStructureEntry entry = ENTITY_STRUCTURES.get(type);
        if (entry == null || !entry.getClazz().equals(clazz))
            return EntityStructureImpl.serialize(entityStructure, serializer);

        return (Map<String, Object>) entry.getSerializer().apply(entityStructure, serializer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends EntityStructure> void validate(@NotNull Map<String, Object> map,
                                                            @NotNull StructureSerializer serializer,
                                                            @NotNull Class<T> clazz)
    {
        EntityType type = getEntityTypeSafe(clazz);
        if (type == EntityType.UNKNOWN)
            type = guessByMapValue(map, type);

        EntityStructureEntry entry = ENTITY_STRUCTURES.get(type);
        if (entry == null)
        {
            EntityStructureImpl.validate(map);
            return;
        }

        entry.getValidator().accept(map, serializer);
    }

    private static EntityType guessByMapValue(@NotNull Map<String, Object> map, EntityType type)
    {
        EntityType typeGuess = Utils.searchEntityType((String) map.get("type"));
        if (typeGuess == null)
            return EntityType.UNKNOWN;
        else
            return typeGuess;

    }

    @NotNull
    private static EntityType getEntityTypeSafe(@NotNull Class<? extends EntityStructure> clazz)
    {
        if (clazz.equals(EntityStructure.class))
            return EntityType.UNKNOWN;  // フォールバックモード

        for (Map.Entry<EntityType, EntityStructureEntry<?, ?>> entry : ENTITY_STRUCTURES.entrySet())
        {
            if (entry.getValue().getClazz().equals(clazz))
                return entry.getKey();
        }

        throw new IllegalArgumentException("Unknown entity structure class: " + clazz);
    }

    private static <E extends Entity, S extends EntityStructure & Mapped> EntityStructureEntry<E, S> getEntry(@NotNull EntityType entityType)
    {
        // noinspection unchecked
        return (EntityStructureEntry<E, S>) ENTITY_STRUCTURES.get(entityType);
    }

    public static <V extends Entity, T extends Mapped & EntityStructure> T toStructure(V value, StructureSerializer structureSerializer)
    {
        if (value == null)
            return null;

        EntityStructureEntry<V, T> entry = getEntry(value.getType());
        if (entry == null)
        {
            // noinspection unchecked: Fallback mode
            return (T) EntityStructureImpl.of(value);
        }

        return entry.getConstructor().apply(value, structureSerializer);
    }

    @Value
    @NotNull
    public static class EntityStructureEntry<E extends Entity, S extends EntityStructure & Mapped>
    {
        Class<S> clazz;
        BiFunction<S, StructureSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, StructureSerializer, S> deserializer;
        BiConsumer<Map<String, Object>, StructureSerializer> validator;
        BiFunction<E, StructureSerializer, S> constructor;
    }
}
