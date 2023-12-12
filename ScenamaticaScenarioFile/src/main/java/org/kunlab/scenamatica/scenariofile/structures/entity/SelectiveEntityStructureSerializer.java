package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.Value;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.AEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
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
    private static final EnumMap<EntityType, EntityStructureEntry<?>> ENTITY_STRUCTURES;

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
                EntityItemStructureImpl::validate
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
                PlayerStructureImpl::validate
        );

        registerStructure(
                EntityType.UNKNOWN,
                AEntityStructure.class,
                AEntityStructureImpl::serialize,
                AEntityStructureImpl::deserialize,
                (stringObjectMap, structureSerializer) -> AEntityStructureImpl.validate(stringObjectMap)
        );

        registerProjectiles();
    }

    private static void registerProjectiles()
    {
        BiFunction<ProjectileStructure, StructureSerializer, Map<String, Object>> serializer = ProjectileStructureImpl::serialize;
        BiFunction<Map<String, Object>, StructureSerializer, ProjectileStructure> deserializer = ProjectileStructureImpl::deserialize;
        BiConsumer<Map<String, Object>, StructureSerializer> validator = ProjectileStructureImpl::validate;

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
                    validator
            );
        }
    }

    private static <S extends EntityStructure> void registerStructure(@NotNull EntityType entityType,
                                                                      @NotNull Class<S> clazz,
                                                                      @NotNull BiFunction<S, StructureSerializer, Map<String, Object>> serializer,
                                                                      @NotNull BiFunction<Map<String, Object>, StructureSerializer, S> deserializer,
                                                                      @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator)
    {
        ENTITY_STRUCTURES.put(
                entityType,
                new EntityStructureEntry<>(
                        clazz,
                        serializer,
                        deserializer,
                        validator
                )
        );
    }

    public static <T extends EntityStructure> T deserialize(@NotNull EntityType entityType,
                                                            @NotNull Map<String, Object> data,
                                                            @NotNull StructureSerializer serializer)
    {
        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(entityType);
        if (entry == null)
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        return entry.getDeserializer().apply(data, serializer);
    }

    public static <T extends EntityStructure> T deserialize(@NotNull Map<String, Object> data,
                                                            @NotNull StructureSerializer serializer,
                                                            @NotNull Class<T> clazz)
    {
        EntityType type = getEntityTypeSafe(clazz);
        type = guessByMapValue(data, type);

        boolean canGeneralize = clazz == EntityStructure.class;
        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(type);
        if (entry == null)
            if (canGeneralize)
                // noinspection unchecked  canGeneralize が true なら, clazz は EntityStructure.class である。
                return (T) AEntityStructureImpl.deserialize(data, serializer);
            else
                throw new IllegalArgumentException("Unknown entity type: " + type);

        return entry.getDeserializer().apply(data, serializer);
    }

    public static <T extends EntityStructure> T deserialize(@NotNull Class<T> clazz,
                                                            @NotNull Map<String, Object> data,
                                                            @NotNull StructureSerializer serializer)
    {
        return deserialize(getEntityTypeSafe(clazz), data, serializer);
    }

    public static <T extends EntityStructure> Map<String, Object> serialize(@NotNull T entityStructure,
                                                                            @NotNull StructureSerializer serializer,
                                                                            @NotNull Class<? extends T> clazz)
    {
        EntityType type = entityStructure.getType();
        if (type == null)
            type = getEntityTypeSafe(clazz);

        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(type);
        if (entry == null || !entry.getClazz().equals(clazz))
            return AEntityStructureImpl.serialize(entityStructure, serializer);

        return entry.getSerializer().apply(entityStructure, serializer);
    }

    public static <T extends EntityStructure> void validate(@NotNull Map<String, Object> map,
                                                            @NotNull StructureSerializer serializer,
                                                            @NotNull Class<T> clazz)
    {
        EntityType type = getEntityTypeSafe(clazz);
        type = guessByMapValue(map, type);

        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(type);
        if (entry == null)
        {
            AEntityStructureImpl.validate(map);
            return;
        }

        entry.getValidator().accept(map, serializer);
    }

    private static EntityType guessByMapValue(@NotNull Map<String, Object> map, EntityType type)
    {
        if (type == EntityType.UNKNOWN)
        {
            EntityType typeGuess = Utils.searchEntityType((String) map.get("type"));
            if (typeGuess != null)
                type = typeGuess;
        }
        return type;
    }

    @NotNull
    private static EntityType getEntityTypeSafe(@NotNull Class<? extends EntityStructure> clazz)
    {
        if (clazz.equals(EntityStructure.class))
            return EntityType.UNKNOWN;  // フォールバックモード

        for (Map.Entry<EntityType, EntityStructureEntry<?>> entry : ENTITY_STRUCTURES.entrySet())
        {
            if (entry.getValue().getClazz().equals(clazz))
                return entry.getKey();
        }

        throw new IllegalArgumentException("Unknown entity structure class: " + clazz);
    }

    @Value
    @NotNull
    public static class EntityStructureEntry<S extends EntityStructure>
    {
        Class<S> clazz;
        BiFunction<S, StructureSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, StructureSerializer, S> deserializer;
        BiConsumer<Map<String, Object>, StructureSerializer> validator;
    }
}