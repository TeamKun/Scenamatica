package org.kunlab.scenamatica.scenariofile;

import lombok.SneakyThrows;
import lombok.Value;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.WitherSkull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.LightningStrikeStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.VehicleStructure;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.PlayerStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.EntityItemStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.LightningStrikeStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.ProjectileStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.VehicleStructureImpl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class SelectiveEntityStructureSerializer
{
    private static final EnumMap<EntityType, EntityStructureEntry<?, ?>> ENTITY_STRUCTURES;

    static
    {
        ENTITY_STRUCTURES = new EnumMap<>(EntityType.class);

        registerEntities();
    }


    @SneakyThrows
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
                EntityType.LIGHTNING,
                LightningStrikeStructure.class,
                LightningStrike.class,
                LightningStrikeStructureImpl::serialize,
                LightningStrikeStructureImpl::deserialize,
                LightningStrikeStructureImpl::validate,
                (lightning, ignored) -> LightningStrikeStructureImpl.ofLightning(lightning)
        );
        registerStructure(
                EntityType.UNKNOWN,
                EntityStructure.class,
                Entity.class,
                EntityStructureImpl::serialize,
                EntityStructureImpl::deserialize,
                (stringObjectMap, structureSerializer) -> {
                    try
                    {
                        EntityStructureImpl.validate(stringObjectMap);
                    }
                    catch (YamlParsingException e)
                    {
                        throw new RuntimeException(e);
                    }
                },
                (entity, ignored) -> EntityStructureImpl.of(entity)
        );

        registerVehicles();
        registerProjectiles();
    }

    @SneakyThrows
    private static void registerProjectiles()
    {
        BiFunction<ProjectileStructure, StructureSerializer, Map<String, Object>> serializer = ProjectileStructureImpl::serialize;
        StructureSerializerImpl.ThrowableBiFunction<StructuredYamlNode, StructureSerializer, ProjectileStructure> deserializer = ProjectileStructureImpl::deserialize;
        StructureSerializerImpl.ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator = ProjectileStructureImpl::validate;
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

    @SneakyThrows
    private static void registerVehicles()
    {
        BiFunction<VehicleStructure, StructureSerializer, Map<String, Object>> serializer = VehicleStructureImpl::serialize;
        StructureSerializerImpl.ThrowableBiFunction<StructuredYamlNode, StructureSerializer, VehicleStructure> deserializer = VehicleStructureImpl::deserialize;
        StructureSerializerImpl.ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator = VehicleStructureImpl::validate;
        BiFunction<Vehicle, StructureSerializer, VehicleStructure> constructor = VehicleStructureImpl::of;

        Map<EntityType, Class<? extends Vehicle>> vehicleTypes = new HashMap<EntityType, Class<? extends Vehicle>>()
        {{
            this.put(EntityType.BOAT, Boat.class);
            this.put(EntityType.MINECART, Minecart.class);
        }};

        for (Map.Entry<EntityType, Class<? extends Vehicle>> vehicleEntry : vehicleTypes.entrySet())
        {
            registerStructure(
                    vehicleEntry.getKey(),
                    VehicleStructure.class,
                    Vehicle.class,
                    serializer,
                    deserializer,
                    validator,
                    constructor
            );
        }
    }

    private static <E extends Entity, S extends EntityStructure> void registerStructure(@NotNull EntityType entityType,
                                                                                        @NotNull Class<S> clazz,
                                                                                        @NotNull Class<? extends E> entityClazz,
                                                                                        @NotNull BiFunction<S, StructureSerializer, Map<String, Object>> serializer,
                                                                                        @NotNull StructureSerializerImpl.ThrowableBiFunction<StructuredYamlNode, StructureSerializer, S> deserializer,
                                                                                        @NotNull StructureSerializerImpl.ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                                                        @NotNull BiFunction<E, StructureSerializer, S> constructor)
    {
        if (!(entityType == EntityType.UNKNOWN || entityClazz.isAssignableFrom(entityType.getEntityClass())))
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

    public static <T extends EntityStructure> T deserialize(@NotNull EntityType entityType,
                                                            @NotNull StructuredYamlNode node,
                                                            @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        // noinspection unchecked
        EntityStructureEntry<?, T> entry = (EntityStructureEntry<?, T>) ENTITY_STRUCTURES.get(entityType);
        if (entry == null)
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        return entry.getDeserializer().apply(node, serializer);
    }

    public static <T extends EntityStructure> T deserialize(@NotNull StructuredYamlNode node,
                                                            @NotNull StructureSerializer serializer,
                                                            @NotNull Class<T> clazz) throws YamlParsingException
    {
        EntityType type = getEntityTypeSafe(clazz);
        if (type == EntityType.UNKNOWN)
            type = guessByMappingNode(node);

        boolean canGeneralize = clazz == EntityStructure.class;
        EntityStructureEntry<?, ?> entry = ENTITY_STRUCTURES.get(type);
        if (entry == null)
            if (canGeneralize)
                // noinspection unchecked  canGeneralize が true なら, clazz は EntityStructure.class である。
                return (T) EntityStructureImpl.deserialize(node, serializer);
            else
                throw new IllegalArgumentException("Unknown entity type: " + type);

        // noinspection unchecked
        return (T) entry.getDeserializer().apply(node, serializer);
    }

    public static <T extends EntityStructure> T deserialize(@NotNull Class<T> clazz,
                                                            @NotNull StructuredYamlNode node,
                                                            @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        return deserialize(getEntityTypeSafe(clazz), node, serializer);
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
    public static <T extends EntityStructure> void validate(@NotNull StructuredYamlNode map,
                                                            @NotNull StructureSerializer serializer,
                                                            @NotNull Class<T> clazz) throws YamlParsingException
    {
        EntityType type = getEntityTypeSafe(clazz);
        if (type == EntityType.UNKNOWN)
            type = guessByMappingNode(map);

        EntityStructureEntry entry = ENTITY_STRUCTURES.get(type);
        if (entry == null)
        {
            EntityStructureImpl.validate(map);
            return;
        }

        entry.getValidator().accept(map, serializer);
    }

    private static EntityType guessByMappingNode(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        if (!node.containsKey("type"))
            return EntityType.UNKNOWN;

        EntityType typeGuess = Utils.searchEntityType(node.get("type").asString());
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

    private static <E extends Entity, S extends EntityStructure> EntityStructureEntry<E, S> getEntry(@NotNull EntityType entityType)
    {
        // noinspection unchecked
        return (EntityStructureEntry<E, S>) ENTITY_STRUCTURES.get(entityType);
    }

    public static <V extends Entity, T extends EntityStructure> T toStructure(V value, StructureSerializerImpl structureSerializer)
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
    public static class EntityStructureEntry<E extends Entity, S extends EntityStructure>
    {
        Class<S> clazz;
        BiFunction<S, StructureSerializer, Map<String, Object>> serializer;
        StructureSerializerImpl.ThrowableBiFunction<StructuredYamlNode, StructureSerializer, S> deserializer;
        StructureSerializerImpl.ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator;
        BiFunction<E, StructureSerializer, S> constructor;
    }
}
