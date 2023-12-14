package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.scenariofile.structures.misc.LocationStructureImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EntityStructureImpl implements EntityStructure
{
    protected final EntityType type;
    protected final LocationStructure location;

    protected final Vector velocity;
    protected final String customName;
    protected final UUID uuid;
    protected final Boolean glowing;
    protected final Boolean gravity;
    protected final Boolean silent;
    protected final Boolean customNameVisible;
    protected final Boolean invulnerable;
    @NotNull
    protected final List<String> tags;
    protected final Integer maxHealth;
    protected final Integer health;
    protected final DamageStructure lastDamageCause;
    protected final Integer fireTicks;
    protected final Integer ticksLived;
    protected final Integer portalCooldown;
    protected final Boolean persistent;
    protected final Float fallDistance;

    protected EntityStructureImpl()
    {
        this(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    protected EntityStructureImpl(@NotNull EntityStructure original)
    {
        this(original.getType(), original);
    }

    protected EntityStructureImpl(@Nullable EntityType type, @NotNull EntityStructure original)
    {
        this(
                type,
                original.getLocation(),
                original.getVelocity(),
                original.getCustomName(),
                original.getUuid(),
                original.getGlowing(),
                original.getGravity(),
                original.getSilent(),
                original.getCustomNameVisible(),
                original.getInvulnerable(),
                new ArrayList<>(original.getTags()),
                original.getMaxHealth(),
                original.getHealth(),
                original.getLastDamageCause(),
                original.getFireTicks(),
                original.getTicksLived(),
                original.getPortalCooldown(),
                original.getPersistent(),
                original.getFallDistance()
        );
    }

    /**
     * エンティティ情報をMapにシリアライズします。
     *
     * @param entity     エンティティ情報
     * @param serializer シリアライザ
     * @return エンティティ情報をシリアライズしたMap
     */
    @NotNull
    public static Map<String, Object> serialize(@NotNull EntityStructure entity, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putAsStrIfNotNull(map, KEY_TYPE, entity.getType());
        if (entity.getLocation() != null)
            map.put(KEY_LOCATION, serializer.serialize(entity.getLocation(), LocationStructure.class));
        if (entity.getVelocity() != null)
            MapUtils.putIfNotNull(map, KEY_VELOCITY, entity.getVelocity().serialize());
        MapUtils.putIfNotNull(map, KEY_CUSTOM_NAME, entity.getCustomName());
        MapUtils.putIfNotNull(map, KEY_GLOWING, entity.getGlowing());
        MapUtils.putIfNotNull(map, KEY_GRAVITY, entity.getGravity());
        MapUtils.putIfNotNull(map, KEY_SILENT, entity.getSilent());
        MapUtils.putIfNotNull(map, KEY_CUSTOM_NAME_VISIBLE, entity.getCustomNameVisible());
        MapUtils.putIfNotNull(map, KEY_INVULNERABLE, entity.getInvulnerable());
        if (entity.getUuid() != null)
            map.put(KEY_UUID, entity.getUuid().toString());
        if (entity.getLastDamageCause() != null)
            map.put(KEY_LAST_DAMAGE_CAUSE, serializer.serialize(entity.getLastDamageCause(), DamageStructure.class));

        MapUtils.putListIfNotEmpty(map, KEY_TAGS, entity.getTags());

        MapUtils.putIfNotNull(map, KEY_MAX_HEALTH, entity.getMaxHealth());
        MapUtils.putIfNotNull(map, KEY_HEALTH, entity.getHealth());


        MapUtils.putIfNotNull(map, KEY_FIRE_TICKS, entity.getFireTicks());
        MapUtils.putIfNotNull(map, KEY_TICKS_LIVED, entity.getTicksLived());
        MapUtils.putIfNotNull(map, KEY_PORTAL_COOLDOWN, entity.getPortalCooldown());
        MapUtils.putIfNotNull(map, KEY_PERSISTENT, entity.getPersistent());
        MapUtils.putIfNotNull(map, KEY_FALL_DISTANCE, entity.getFallDistance());

        return map;
    }

    /**
     * Mapが正しいエンティティ情報かどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException Mapが正しいエンティティ情報ではない場合
     */
    public static void validate(@NotNull Map<String, Object> map)
    {
        if (map.containsKey(KEY_UUID))
            try
            {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString((String) map.get(KEY_UUID));
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException("Invalid UUID.", e);
            }

        MapUtils.checkTypeIfContains(map, KEY_CUSTOM_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_GLOWING, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_GRAVITY, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_TAGS, List.class);
        MapUtils.checkTypeIfContains(map, KEY_LAST_DAMAGE_CAUSE, Map.class);
        MapUtils.checkTypeIfContains(map, KEY_MAX_HEALTH, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_HEALTH, Integer.class);

    }

    /**
     * Mapからエンティティ情報をデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズしたエンティティ情報
     */
    @NotNull
    public static EntityStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map);

        EntityType type = null;
        if (map.containsKey(KEY_TYPE))
            type = Utils.searchEntityType((String) map.get(KEY_TYPE));
        LocationStructure loc;
        if (map.containsKey(KEY_LOCATION))
            loc = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_LOCATION)), LocationStructure.class);
        else if (map.containsKey(KEY_LOCATION_2))
            loc = LocationStructureImpl.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_LOCATION_2)));
        else
            loc = null;

        Vector velocity = null;
        if (map.containsKey(KEY_VELOCITY))
            velocity = Vector.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_VELOCITY)));

        String customName = MapUtils.getOrNull(map, KEY_CUSTOM_NAME);
        UUID uuid;
        if (map.containsKey(KEY_UUID))
            uuid = UUID.fromString((String) map.get(KEY_UUID));
        else
            uuid = null;

        Boolean glowing = MapUtils.getOrNull(map, KEY_GLOWING);
        Boolean gravity = MapUtils.getOrNull(map, KEY_GRAVITY);
        Boolean silent = MapUtils.getOrNull(map, KEY_SILENT);
        Boolean customNameVisible = MapUtils.getOrNull(map, KEY_CUSTOM_NAME_VISIBLE);
        Boolean invulnerable = MapUtils.getOrNull(map, KEY_INVULNERABLE);

        List<String> tags = MapUtils.getAsListOrEmpty(map, KEY_TAGS);
        DamageStructure lastDamageCause = null;
        if (map.containsKey(KEY_LAST_DAMAGE_CAUSE))
            lastDamageCause = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_LAST_DAMAGE_CAUSE)), DamageStructure.class);

        Integer maxHealth = MapUtils.getOrNull(map, KEY_MAX_HEALTH);
        Integer health = MapUtils.getOrNull(map, KEY_HEALTH);

        Integer fireTicks = MapUtils.getOrNull(map, KEY_FIRE_TICKS);
        Integer ticksLived = MapUtils.getOrNull(map, KEY_TICKS_LIVED);
        Integer portalCooldown = MapUtils.getOrNull(map, KEY_PORTAL_COOLDOWN);
        Boolean persistent = MapUtils.getOrNull(map, KEY_PERSISTENT);
        Number fallDistanceNumber = MapUtils.getAsNumberOrNull(map, KEY_FALL_DISTANCE);
        Float fallDistance = null;
        if (fallDistanceNumber != null)
            fallDistance = fallDistanceNumber.floatValue();

        return new EntityStructureImpl(
                type,
                loc,
                velocity,
                customName,
                uuid,
                glowing,
                gravity,
                silent,
                customNameVisible,
                invulnerable,
                tags,
                maxHealth,
                health,
                lastDamageCause,
                fireTicks,
                ticksLived,
                portalCooldown,
                persistent,
                fallDistance
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof EntityStructureImpl)) return false;
        EntityStructureImpl that = (EntityStructureImpl) o;
        return this.glowing == that.glowing
                && this.gravity == that.gravity
                && this.type == that.type
                && Objects.equals(this.location, that.location)
                && Objects.equals(this.customName, that.customName)
                && Objects.equals(this.uuid, that.uuid)
                && Objects.equals(this.tags, that.tags)
                && Objects.equals(this.lastDamageCause, that.lastDamageCause)
                && Objects.equals(this.maxHealth, that.maxHealth)
                && Objects.equals(this.health, that.health);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.type, this.location, this.customName, this.uuid, this.glowing, this.gravity,
                this.tags, this.lastDamageCause, this.maxHealth, this.health
        );
    }

    protected void applyToEntity(Entity entity)
    {
        if (this.location != null)
        {
            if (this.location.getWorld() == null)
                entity.teleport(this.location.changeWorld(entity.getWorld().getName()).create());
            else
                entity.teleport(this.location.create());
        }
        if (this.customName != null)
            entity.setCustomName(this.customName);
        if (this.velocity != null)
            entity.setVelocity(this.velocity);
        if (this.customNameVisible != null)
            entity.setCustomNameVisible(this.customNameVisible);
        if (this.glowing != null)
            entity.setGlowing(this.glowing);
        if (this.gravity != null)
            entity.setGravity(this.gravity);
        if (this.silent != null)
            entity.setSilent(this.silent);
        if (this.invulnerable != null)
            entity.setInvulnerable(this.invulnerable);
        if (this.customNameVisible != null)
            entity.setCustomNameVisible(this.customNameVisible);
        if (this.invulnerable != null)
            entity.setInvulnerable(this.invulnerable);
        if (!this.tags.isEmpty())
        {
            entity.getScoreboardTags().clear();
            entity.getScoreboardTags().addAll(this.tags);
        }
        if (this.lastDamageCause != null)
            entity.setLastDamageCause(new EntityDamageEvent(
                            entity,
                            this.lastDamageCause.getCause(),
                            this.lastDamageCause.getDamage()
                    )
            );
        if (entity instanceof Damageable)
        {
            if (this.maxHealth != null)
                // noinspection deprecation
                ((Damageable) entity).setMaxHealth(this.maxHealth);
            if (this.health != null)
                ((Damageable) entity).setHealth(this.health);
        }

        if (this.fireTicks != null)
            entity.setFireTicks(this.fireTicks);
        if (this.ticksLived != null)
            entity.setTicksLived(this.ticksLived);
        if (this.portalCooldown != null)
            entity.setPortalCooldown(this.portalCooldown);
        if (this.persistent != null)
            entity.setPersistent(this.persistent);
        if (this.fallDistance != null)
            entity.setFallDistance(this.fallDistance);
    }

    protected boolean isAdequateEntity(Entity entity, boolean strict)
    {
        if (!this.tags.isEmpty())
        {
            ArrayList<String> tags = new ArrayList<>(entity.getScoreboardTags());
            if (strict && tags.size() != this.tags.size())
                return false;
            if (!tags.containsAll(this.tags))
                return false;
        }

        if (this.lastDamageCause != null)
        {
            EntityDamageEvent lastDamageCause = entity.getLastDamageCause();
            if (lastDamageCause == null)
                return false;
            if (lastDamageCause.getCause() != this.lastDamageCause.getCause())
                return false;
            if (lastDamageCause.getDamage() != this.lastDamageCause.getDamage())
                return false;
        }

        if (entity instanceof Damageable)
        {
            if (this.maxHealth != null)
                // noinspection deprecation
                if (((Damageable) entity).getMaxHealth() != this.maxHealth)
                    return false;
            if (this.health != null)
                if (((Damageable) entity).getHealth() != this.health)
                    return false;
        }

        return (this.type == null || entity.getType() == this.type)
                && (this.customName == null || this.customName.equals(entity.getCustomName()))
                && (this.location == null || this.location.isAdequate(entity.getLocation(), strict))
                && (this.velocity == null || this.velocity.equals(entity.getVelocity()))
                && (this.uuid == null || this.uuid.equals(entity.getUniqueId()))
                && (this.glowing == null || this.glowing.equals(entity.isGlowing()))
                && (this.gravity == null || this.gravity.equals(entity.hasGravity()))
                && (this.silent == null || this.silent.equals(entity.isSilent()))
                && (this.customNameVisible == null || this.customNameVisible.equals(entity.isCustomNameVisible()))
                && (this.invulnerable == null || this.invulnerable.equals(entity.isInvulnerable()))
                && (this.fireTicks == null || this.fireTicks.equals(entity.getFireTicks()))
                && (this.ticksLived == null || this.ticksLived.equals(entity.getTicksLived()))
                && (this.portalCooldown == null || this.portalCooldown.equals(entity.getPortalCooldown()))
                && (this.persistent == null || this.persistent.equals(entity.isPersistent()))
                && (this.fallDistance == null || this.fallDistance.equals(entity.getFallDistance()));
    }
}
