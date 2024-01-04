package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.Value;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.AEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.PlayerStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.AEntityStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.EntityItemStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.ProjectileStructureImpl;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SelectiveEntityStructureSerializer
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
                EntityItemStructureImpl::serialize,
                EntityItemStructureImpl::deserialize,
                EntityItemStructureImpl::validate,
                (entity, ignored) -> EntityItemStructureImpl.of(entity)
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
                PlayerStructureImpl::serialize,
                PlayerStructureImpl::deserialize,
                PlayerStructureImpl::validate,
                (player, ignored) -> PlayerStructureImpl.of(player)
        );

        registerStructure(
                EntityType.UNKNOWN,
                AEntityStructure.class,
                AEntityStructureImpl::serialize,
                AEntityStructureImpl::deserialize,
                (stringObjectMap, structureSerializer) -> AEntityStructureImpl.validate(stringObjectMap),
                (entity, ignored) -> AEntityStructureImpl.of(entity)
        );

        registerProjectiles();
    }

    private static void registerProjectiles()
    {
        BiFunction<ProjectileStructure, StructureSerializer, Map<String, Object>> serializer = ProjectileStructureImpl::serialize;
        BiFunction<Map<String, Object>, StructureSerializer, ProjectileStructure> deserializer = ProjectileStructureImpl::deserialize;
        BiConsumer<Map<String, Object>, StructureSerializer> validator = ProjectileStructureImpl::validate;
        BiFunction<Projectile, StructureSerializer, ProjectileStructure> constructor = ProjectileStructureImpl::of;

        EntityType[] projectileTypes = {
                EntityType.ARROW,
                EntityType.DRAGON_FIREBALL,
                EntityType.EGG,
                EntityType.ENDER_PEARL,
                EntityType.SMALL_FIREBALL, // CraftBukkit: Fireball
                EntityType.FIREWORK,
                EntityType.FISHING_HOOK,
                EntityType.FIREBALL, // CraftBukkit: LargeFireball
                EntityType.LLAMA_SPIT,
                EntityType.SHULKER_BULLET,
                EntityType.SNOWBALL,
                EntityType.SPECTRAL_ARROW,
                EntityType.THROWN_EXP_BOTTLE,
                EntityType.THROWN_EXP_BOTTLE,
                EntityType.SPLASH_POTION,
                EntityType.TRIDENT,
                EntityType.WITHER_SKULL,
        };

        for (EntityType projectileType : projectileTypes)
        {
            registerStructure(
                    projectileType,
                    ProjectileStructure.class,
                    serializer,
                    deserializer,
                    validator,
                    constructor
            );
        }
    }

    private static <E extends Entity, S extends EntityStructure & Mapped<E>> void registerStructure(@NotNull EntityType entityType,
                                                                                                    @NotNull Class<S> clazz,
                                                                                                    @NotNull BiFunction<S, StructureSerializer, Map<String, Object>> serializer,
                                                                                                    @NotNull BiFunction<Map<String, Object>, StructureSerializer, S> deserializer,
                                                                                                    @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator,
                                                                                                    @NotNull BiFunction<E, StructureSerializer, S> constructor)
    {
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

    public static <T extends EntityStructure & Mapped<?>> T deserialize(@NotNull EntityType entityType,
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

        boolean canGeneralize = clazz == EntityStructure.class || clazz == AEntityStructure.class;
        EntityStructureEntry<?, ?> entry = ENTITY_STRUCTURES.get(type);
        if (entry == null)
            if (canGeneralize)
                // noinspection unchecked  canGeneralize が true なら, clazz は EntityStructure.class である。
                return (T) AEntityStructureImpl.deserialize(data, serializer);
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
            return AEntityStructureImpl.serialize(entityStructure, serializer);

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
            AEntityStructureImpl.validate(map);
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
        if (clazz.equals(EntityStructure.class) || clazz.equals(AEntityStructure.class))
            return EntityType.UNKNOWN;  // フォールバックモード

        for (Map.Entry<EntityType, EntityStructureEntry<?, ?>> entry : ENTITY_STRUCTURES.entrySet())
        {
            if (entry.getValue().getClazz().equals(clazz))
                return entry.getKey();
        }

        throw new IllegalArgumentException("Unknown entity structure class: " + clazz);
    }

    private static <E extends Entity, S extends EntityStructure & Mapped<E>> EntityStructureEntry<E, S> getEntry(@NotNull EntityType entityType)
    {
        // noinspection unchecked
        return (EntityStructureEntry<E, S>) ENTITY_STRUCTURES.get(entityType);
    }

    public static <V extends Entity, T extends Mapped<V> & EntityStructure> T toStructure(V value, StructureSerializerImpl structureSerializer)
    {
        if (value == null)
            return null;

        EntityStructureEntry<V, T> entry = getEntry(value.getType());
        if (entry == null)
        {
            // noinspection unchecked: Fallback mode
            return (T) AEntityStructureImpl.of(value);
        }

        return entry.getConstructor().apply(value, structureSerializer);
    }

    @Value
    @NotNull
    public static class EntityStructureEntry<E extends Entity, S extends EntityStructure & Mapped<E>>
    {
        Class<S> clazz;
        BiFunction<S, StructureSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, StructureSerializer, S> deserializer;
        BiConsumer<Map<String, Object>, StructureSerializer> validator;
        BiFunction<E, StructureSerializer, S> constructor;
    }
}
