package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.Value;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.GenericEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.scenariofile.structures.context.PlayerStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.EntityItemStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.ProjectileStructureImpl;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SelectingEntityStructureSerializer
{
    private static final EntityStructureEntry<?> FALLBACK_ENTITY_STRUCTURE_ENTRY = new EntityStructureEntry<>(
            EntityStructure.class,
            EntityStructureImpl::serialize,
            EntityStructureImpl::deserialize,
            (stringObjectMap, structureSerializer) -> EntityStructureImpl.validate(stringObjectMap)
    );

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

    private static <S extends GenericEntityStructure> void registerStructure(@NotNull EntityType entityType,
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

    public static <E extends Entity, T extends GenericEntityStructure> T deserialize(@NotNull EntityType entityType,
                                                                                     @NotNull Map<String, Object> data,
                                                                                     @NotNull StructureSerializer serializer)
    {
        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(entityType);
        if (entry == null)
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        return entry.getDeserializer().apply(data, serializer);
    }

    public static <E extends Entity, T extends GenericEntityStructure> T deserialize(@NotNull Map<String, Object> data,
                                                                                     @NotNull StructureSerializer serializer)
    {
        EntityType type = MapUtils.getAsEnumOrNull(data, GenericEntityStructure.KEY_TYPE, EntityType.class);
        if (type == null)
            // noinspection unchecked
            return (T) FALLBACK_ENTITY_STRUCTURE_ENTRY.getDeserializer().apply(data, serializer);

        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(type);
        if (entry == null)
            throw new IllegalArgumentException("Unknown entity type: " + type);

        return entry.getDeserializer().apply(data, serializer);
    }

    public static <T extends GenericEntityStructure> T deserialize(@NotNull Class<T> clazz,
                                                                   @NotNull Map<String, Object> data,
                                                                   @NotNull StructureSerializer serializer)
    {
        return deserialize(getEntityTypeSafe(clazz), data, serializer);
    }

    public static <E extends Entity, T extends GenericEntityStructure> Map<String, Object> serialize(@NotNull T entityStructure,
                                                                                                     @NotNull StructureSerializer serializer)
    {
        EntityType type = entityStructure.getType();
        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(type);
        if (entry == null)
            return EntityStructureImpl.serialize(entityStructure, serializer);
        return entry.getSerializer().apply(entityStructure, serializer);
    }

    public static <T extends GenericEntityStructure> void validate(@NotNull Map<String, Object> map,
                                                                   @NotNull StructureSerializer serializer)
    {
        EntityType type = MapUtils.getAsEnumOrNull(map, GenericEntityStructure.KEY_TYPE, EntityType.class);
        if (type == null)
            FALLBACK_ENTITY_STRUCTURE_ENTRY.getValidator().accept(map, serializer);

        // noinspection unchecked
        EntityStructureEntry<T> entry = (EntityStructureEntry<T>) ENTITY_STRUCTURES.get(type);
        if (entry == null)
        {
            FALLBACK_ENTITY_STRUCTURE_ENTRY.getValidator().accept(map, serializer);
            return;
        }

        entry.getValidator().accept(map, serializer);
    }

    @NotNull
    private static EntityType getEntityTypeSafe(@NotNull Class<? extends GenericEntityStructure> clazz)
    {
        for (Map.Entry<EntityType, EntityStructureEntry<?>> entry : ENTITY_STRUCTURES.entrySet())
        {
            if (entry.getValue().getClazz().equals(clazz))
                return entry.getKey();
        }

        throw new IllegalArgumentException("Unknown entity structure class: " + clazz);
    }

    @Value
    @NotNull
    public static class EntityStructureEntry<S extends GenericEntityStructure>
    {
        Class<S> clazz;
        BiFunction<S, StructureSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, StructureSerializer, S> deserializer;
        BiConsumer<Map<String, Object>, StructureSerializer> validator;
    }
}
