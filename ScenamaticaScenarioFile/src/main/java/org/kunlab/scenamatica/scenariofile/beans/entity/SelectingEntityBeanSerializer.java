package org.kunlab.scenamatica.scenariofile.beans.entity;

import lombok.Value;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileBean;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.EntityItemBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.HumanEntityBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.ProjectileBeanImpl;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SelectingEntityBeanSerializer
{
    private static final EntityBeanEntry<EntityBean> FALLBACK_ENTITY_BEAN_ENTRY = new EntityBeanEntry<>(
            EntityBean.class,
            EntityItemBeanImpl::serialize,
            EntityItemBeanImpl::deserialize,
            EntityItemBeanImpl::validate
    );

    private static final EnumMap<EntityType, EntityBeanEntry<? extends EntityBean>> ENTITY_BEANS;

    static
    {
        ENTITY_BEANS = new EnumMap<>(EntityType.class);

        registerEntities();
    }


    private static void registerEntities()
    {
        registerBean(
                EntityType.DROPPED_ITEM,
                EntityItemBean.class,
                EntityItemBeanImpl::serialize,
                EntityItemBeanImpl::deserialize,
                EntityItemBeanImpl::validate
        );
        registerBean(
                EntityType.PLAYER,
                HumanEntityBean.class,
                HumanEntityBeanImpl::serialize,
                HumanEntityBeanImpl::deserialize,
                HumanEntityBeanImpl::validate
        );

        registerProjectiles();
    }

    private static void registerProjectiles()
    {
        BiFunction<ProjectileBean, BeanSerializer, Map<String, Object>> serializer = ProjectileBeanImpl::serialize;
        BiFunction<Map<String, Object>, BeanSerializer, ProjectileBean> deserializer = ProjectileBeanImpl::deserialize;
        BiConsumer<Map<String, Object>, BeanSerializer> validator = ProjectileBeanImpl::validate;

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
            registerBean(
                    projectileType,
                    ProjectileBean.class,
                    serializer,
                    deserializer,
                    validator
            );
        }
    }

    private static <T extends EntityBean> void registerBean(@NotNull EntityType entityType,
                                                            @NotNull Class<T> clazz,
                                                            @NotNull BiFunction<T, BeanSerializer, Map<String, Object>> serializer,
                                                            @NotNull BiFunction<Map<String, Object>, BeanSerializer, T> deserializer,
                                                            @NotNull BiConsumer<Map<String, Object>, BeanSerializer> validator)
    {
        ENTITY_BEANS.put(
                entityType,
                new EntityBeanEntry<>(
                        clazz,
                        serializer,
                        deserializer,
                        validator
                )
        );
    }

    public static <T extends EntityBean> T deserialize(@NotNull EntityType entityType,
                                                       @NotNull Map<String, Object> data,
                                                       @NotNull BeanSerializer serializer)
    {
        // noinspection unchecked
        EntityBeanEntry<T> entry = (EntityBeanEntry<T>) ENTITY_BEANS.get(entityType);
        if (entry == null)
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        return entry.getDeserializer().apply(data, serializer);
    }

    public static <T extends EntityBean> T deserialize(@NotNull Map<String, Object> data,
                                                       @NotNull BeanSerializer serializer)
    {
        EntityType type = MapUtils.getAsEnumOrNull(data, EntityBean.KEY_TYPE, EntityType.class);
        if (type == null)
            // noinspection unchecked
            return (T) FALLBACK_ENTITY_BEAN_ENTRY.getDeserializer().apply(data, serializer);

        // noinspection unchecked
        EntityBeanEntry<T> entry = (EntityBeanEntry<T>) ENTITY_BEANS.get(type);
        if (entry == null)
            throw new IllegalArgumentException("Unknown entity type: " + type);

        return entry.getDeserializer().apply(data, serializer);
    }

    public static <T extends EntityBean> T deserialize(@NotNull Class<T> clazz,
                                                       @NotNull Map<String, Object> data,
                                                       @NotNull BeanSerializer serializer)
    {
        return deserialize(getEntityTypeSafe(clazz), data, serializer);
    }

    public static <T extends EntityBean> Map<String, Object> serialize(@NotNull T entityBean,
                                                                       @NotNull BeanSerializer serializer)
    {
        EntityType type = entityBean.getType();
        // noinspection unchecked
        EntityBeanEntry<T> entry = (EntityBeanEntry<T>) ENTITY_BEANS.get(type);
        if (entry == null)
            return FALLBACK_ENTITY_BEAN_ENTRY.getSerializer().apply(entityBean, serializer);
        return entry.getSerializer().apply(entityBean, serializer);
    }

    public static <T extends EntityBean> void validate(@NotNull Map<String, Object> map,
                                                       @NotNull BeanSerializer serializer)
    {
        EntityType type = MapUtils.getAsEnumOrNull(map, EntityBean.KEY_TYPE, EntityType.class);
        if (type == null)
            FALLBACK_ENTITY_BEAN_ENTRY.getValidator().accept(map, serializer);

        // noinspection unchecked
        EntityBeanEntry<T> entry = (EntityBeanEntry<T>) ENTITY_BEANS.get(type);
        if (entry == null)
        {
            FALLBACK_ENTITY_BEAN_ENTRY.getValidator().accept(map, serializer);
            return;
        }

        entry.getValidator().accept(map, serializer);
    }

    @NotNull
    private static EntityType getEntityTypeSafe(@NotNull Class<? extends EntityBean> clazz)
    {
        for (Map.Entry<EntityType, EntityBeanEntry<? extends EntityBean>> entry : ENTITY_BEANS.entrySet())
        {
            if (entry.getValue().getClazz().equals(clazz))
                return entry.getKey();
        }

        throw new IllegalArgumentException("Unknown entity bean class: " + clazz);
    }

    @Value
    @NotNull
    public static class EntityBeanEntry<T extends EntityBean>
    {
        Class<T> clazz;
        BiFunction<T, BeanSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, BeanSerializer, T> deserializer;
        BiConsumer<Map<String, Object>, BeanSerializer> validator;
    }
}
