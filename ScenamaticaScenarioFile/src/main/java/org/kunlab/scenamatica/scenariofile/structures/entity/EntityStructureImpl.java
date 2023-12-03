package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

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
    protected final Location location;

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
    @NotNull
    protected final List<PotionEffect> potionEffects;
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
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                null
        );
    }

    protected EntityStructureImpl(@NotNull EntityStructure original)
    {
        this(
                original.getType(),
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
                new ArrayList<>(original.getPotionEffects()),
                original.getFireTicks(),
                original.getTicksLived(),
                original.getPortalCooldown(),
                original.getPersistent(),
                original.getFallDistance()
        );
    }

    protected EntityStructureImpl(@NotNull EntityType type, @NotNull EntityStructure original)
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
                new ArrayList<>(original.getPotionEffects()),
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
        MapUtils.putLocationIfNotNull(map, KEY_LOCATION, entity.getLocation());
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
            map.put(KEY_LAST_DAMAGE, serializer.serialize(entity.getLastDamageCause(), DamageStructure.class));

        MapUtils.putListIfNotEmpty(map, KEY_TAGS, entity.getTags());

        MapUtils.putIfNotNull(map, KEY_MAX_HEALTH, entity.getMaxHealth());
        MapUtils.putIfNotNull(map, KEY_HEALTH, entity.getHealth());

        if (!entity.getPotionEffects().isEmpty())
            map.put(KEY_POTION_EFFECTS, serializePotionEffects(entity.getPotionEffects()));

        MapUtils.putIfNotNull(map, KEY_FIRE_TICKS, entity.getFireTicks());
        MapUtils.putIfNotNull(map, KEY_TICKS_LIVED, entity.getTicksLived());
        MapUtils.putIfNotNull(map, KEY_PORTAL_COOLDOWN, entity.getPortalCooldown());
        MapUtils.putIfNotNull(map, KEY_PERSISTENT, entity.getPersistent());
        MapUtils.putIfNotNull(map, KEY_FALL_DISTANCE, entity.getFallDistance());

        return map;
    }

    private static List<Map<String, Object>> serializePotionEffects(@NotNull List<? extends PotionEffect> potionEffects)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PotionEffect potionEffect : potionEffects)
        {
            Map<String, Object> potionEffectMap = new HashMap<>();

            if (!potionEffect.isAmbient())
                potionEffectMap.put(KEY_POTION_EFFECTS_AMBIENT, false);
            if (potionEffect.getAmplifier() != 0)
                potionEffectMap.put(KEY_POTION_EFFECTS_AMPLIFIER, potionEffect.getAmplifier());
            if (!potionEffect.hasParticles())
                potionEffectMap.put(KEY_POTION_EFFECTS_SHOW_PARTICLES, false);
            if (!potionEffect.hasIcon())
                potionEffectMap.put(KEY_POTION_EFFECTS_SHOW_ICON, false);
            if (potionEffect.getDuration() != 0)
                potionEffectMap.put(KEY_POTION_EFFECTS_DURATION, potionEffect.getDuration());
            potionEffectMap.put(KEY_POTION_EFFECTS_TYPE, potionEffect.getType().getName());

            list.add(potionEffectMap);
        }

        return list;
    }

    private static void validatePotionEffectMap(@NotNull List<Map<String, Object>> map)
    {
        if (map.isEmpty())
            return;

        for (Object o : map)
        {
            Map<String, Object> effectMap =
                    MapUtils.checkAndCastMap(o);
            MapUtils.checkType(effectMap, KEY_POTION_EFFECTS_TYPE, String.class);
            if (PotionEffectType.getByName((String) effectMap.get(KEY_POTION_EFFECTS_TYPE)) == null)
                throw new IllegalArgumentException("Invalid potion effect type.");

            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_AMBIENT, Boolean.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_AMPLIFIER, Integer.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_DURATION, Integer.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_SHOW_ICON, Boolean.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_SHOW_PARTICLES, Boolean.class);
        }
    }

    private static List<PotionEffect> deserializePotionEffects(@NotNull List<? extends Map<String, Object>> map)
    {
        List<PotionEffect> list = new ArrayList<>();
        for (Map<String, Object> effectMap : map)
        {
            PotionEffectType type =
                    PotionEffectType.getByName((String) effectMap.get(KEY_POTION_EFFECTS_TYPE));

            long duration = 0L;
            if (effectMap.containsKey(KEY_POTION_EFFECTS_DURATION))
                duration = Long.parseLong(effectMap.get(KEY_POTION_EFFECTS_DURATION).toString());
            int amplifier = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_AMPLIFIER, 0);
            boolean ambient = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_AMBIENT, false);
            boolean particles = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_SHOW_PARTICLES, true);
            boolean icon = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_SHOW_ICON, true);

            assert type != null;  // validatePotionEffectMapで検証済み
            list.add(new PotionEffect(type, (int) duration, amplifier, ambient, particles, icon));
        }

        return list;
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

        MapUtils.checkLocationIfContains(map, KEY_LOCATION);
        MapUtils.checkTypeIfContains(map, KEY_CUSTOM_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_GLOWING, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_GRAVITY, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_TAGS, List.class);
        MapUtils.checkTypeIfContains(map, KEY_LAST_DAMAGE, Map.class);
        MapUtils.checkTypeIfContains(map, KEY_MAX_HEALTH, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_HEALTH, Integer.class);

        if (map.containsKey(KEY_POTION_EFFECTS))
        {
            MapUtils.checkType(map, KEY_POTION_EFFECTS, List.class);
            validatePotionEffectMap(MapUtils.getAsList(map, KEY_POTION_EFFECTS));
        }
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
        Location loc = MapUtils.getAsLocationOrNull(map, KEY_LOCATION);
        if (loc == null)
            loc = MapUtils.getAsLocationOrNull(map, KEY_LOCATION_2);

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
        if (map.containsKey(KEY_LAST_DAMAGE))
            lastDamageCause = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_LAST_DAMAGE)), DamageStructure.class);

        Integer maxHealth = MapUtils.getOrNull(map, KEY_MAX_HEALTH);
        Integer health = MapUtils.getOrNull(map, KEY_HEALTH);

        List<PotionEffect> potionEffects = new ArrayList<>();
        if (map.containsKey(KEY_POTION_EFFECTS))
            potionEffects = deserializePotionEffects(MapUtils.getAsList(map, KEY_POTION_EFFECTS));

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
                potionEffects,
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
        if (entity instanceof LivingEntity)
        {
            if (!this.potionEffects.isEmpty())
            {
                new ArrayList<>(((LivingEntity) entity).getActivePotionEffects()).stream()
                        .map(PotionEffect::getType)
                        .forEach(((LivingEntity) entity)::removePotionEffect);

                this.potionEffects.stream()
                        .map(b -> new PotionEffect(
                                        b.getType(),
                                        b.getDuration(),
                                        b.getAmplifier(),
                                        b.isAmbient(),
                                        b.hasParticles(),
                                        b.hasIcon()
                                )
                        )
                        .forEach(((LivingEntity) entity)::addPotionEffect);
            }
            if (this.fireTicks != null)
                entity.setFireTicks(this.fireTicks);
            if (this.ticksLived != null)
                entity.setTicksLived(this.ticksLived);
            if (this.portalCooldown != null)
                entity.setPortalCooldown(this.portalCooldown);
            if (this.persistent != null)
            {
                entity.setPersistent(this.persistent);
                if (this.fallDistance != null)
                    entity.setFallDistance(this.fallDistance);
            }
        }
    }

    protected boolean isAdequateEntity(Entity entity, boolean strict)
    {

        if (this.type != null)
            if (entity.getType() != this.type)
                return false;
        if (this.customName != null)
            if (!Objects.equals(entity.getCustomName(), this.customName))
                return false;
        if (this.velocity != null)
            if (!entity.getVelocity().equals(this.velocity))
                return false;
        if (this.customNameVisible != null)
            if (entity.isCustomNameVisible() != this.customNameVisible)
                return false;
        if (this.glowing != null)
            if (entity.isGlowing() != this.glowing)
                return false;
        if (this.gravity != null)
            if (entity.hasGravity() != this.gravity)
                return false;
        if (this.silent != null)
            if (entity.isSilent() != this.silent)
                return false;
        if (this.invulnerable != null)
            if (entity.isInvulnerable() != this.invulnerable)
                return false;
        if (this.customNameVisible != null)
            if (entity.isCustomNameVisible() != this.customNameVisible)
                return false;
        if (this.invulnerable != null)
            if (entity.isInvulnerable() != this.invulnerable)
                return false;
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

        if (entity instanceof LivingEntity)
            if (!this.potionEffects.isEmpty())
            {
                List<PotionEffect> potionEffects = new ArrayList<>(((LivingEntity) entity).getActivePotionEffects());
                if (strict && potionEffects.size() != this.potionEffects.size())
                    return false;

                for (PotionEffect effects : this.potionEffects)
                    if (!potionEffects.contains(effects))
                        return false;
            }

        if (this.fireTicks != null)
            if (entity.getFireTicks() != this.fireTicks)
                return false;
        if (this.ticksLived != null)
            if (entity.getTicksLived() != this.ticksLived)
                return false;
        if (this.portalCooldown != null)
            if (entity.getPortalCooldown() != this.portalCooldown)
                return false;
        if (this.persistent != null)
            if (entity.isPersistent() != this.persistent)
                return false;

        if (this.fallDistance != null)
            // noinspection RedundantIfStatement
            if (entity.getFallDistance() != this.fallDistance)
                return false;

        return true;
    }
}
