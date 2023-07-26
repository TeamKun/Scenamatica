package org.kunlab.scenamatica.scenariofile.beans.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.DamageBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EntityBeanImpl implements EntityBean
{
    private final EntityType type;
    private final Location location;

    private final Vector velocity;
    private final String customName;
    private final UUID uuid;
    private final Boolean glowing;
    private final Boolean gravity;
    private final Boolean silent;
    private final Boolean customNameVisible;
    private final Boolean invulnerable;
    @NotNull
    private final List<String> tags;
    private final Integer maxHealth;
    private final Integer health;
    private final DamageBean lastDamageCause;
    @NotNull
    private final List<PotionEffect> potionEffects;
    private final Integer fireTicks;
    private final Integer ticksLived;
    private final Integer portalCooldown;
    private final Boolean persistent;
    private final Float fallDistance;

    public EntityBeanImpl()
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

    /**
     * エンティティ情報をMapにシリアライズします。
     *
     * @param entity     エンティティ情報
     * @param serializer シリアライザ
     * @return エンティティ情報をシリアライズしたMap
     */
    @NotNull
    public static Map<String, Object> serialize(@NotNull EntityBean entity, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotNull(map, KEY_TYPE, entity.getType());
        MapUtils.putLocationIfNotNull(map, KEY_LOCATION, entity.getLocation());
        MapUtils.putIfNotNull(map, KEY_VELOCITY, entity.getVelocity());
        MapUtils.putIfNotNull(map, KEY_CUSTOM_NAME, entity.getCustomName());
        MapUtils.putIfNotNull(map, KEY_GLOWING, entity.getGlowing());
        MapUtils.putIfNotNull(map, KEY_GRAVITY, entity.getGravity());
        MapUtils.putIfNotNull(map, KEY_SILENT, entity.getSilent());
        MapUtils.putIfNotNull(map, KEY_CUSTOM_NAME_VISIBLE, entity.getCustomNameVisible());
        MapUtils.putIfNotNull(map, KEY_INVULNERABLE, entity.getInvulnerable());
        if (entity.getUuid() != null)
            map.put(KEY_UUID, entity.getUuid().toString());
        if (entity.getLastDamageCause() != null)
            map.put(EntityBean.KEY_LAST_DAMAGE, serializer.serializeDamage(entity.getLastDamageCause()));

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
            Map<String, Object> effectMap = MapUtils.checkAndCastMap(
                    o,
                    String.class,
                    Object.class
            );
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
    @SuppressWarnings("deprecation")
    public static EntityBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map);

        EntityType type = null;
        if (map.containsKey(KEY_TYPE))
        {
            String key = (String) map.get(KEY_TYPE);
            if (key.contains(":"))
                key = key.split(":")[1];

            type = EntityType.fromName(key);
            if (type == null)
                try
                {
                    type = EntityType.fromId(Integer.parseInt(key));
                }
                catch (NumberFormatException ignored)
                {
                }
            if (type == null)
                try
                {
                    type = EntityType.valueOf(key);
                }
                catch (IllegalArgumentException ignored)
                {
                    throw new IllegalArgumentException("Invalid entity type: " + key);
                }
        }
        Location loc = MapUtils.getAsLocationOrNull(map, KEY_LOCATION);
        if (loc == null)
            loc = MapUtils.getAsLocationOrNull(map, KEY_LOCATION_2);

        Vector velocity = null;
        if (map.containsKey(KEY_VELOCITY))
            velocity = Vector.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_VELOCITY),
                    String.class,
                    Object.class
            ));

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
        DamageBean lastDamageCause = null;
        if (map.containsKey(KEY_LAST_DAMAGE))
            lastDamageCause = serializer.deserializeDamage(MapUtils.checkAndCastMap(map.get(KEY_LAST_DAMAGE),
                    String.class, Object.class
            ));

        Integer maxHealth = MapUtils.getOrNull(map, KEY_MAX_HEALTH);
        Integer health = MapUtils.getOrNull(map, KEY_HEALTH);

        List<PotionEffect> potionEffects = new ArrayList<>();
        if (map.containsKey(KEY_POTION_EFFECTS))
            potionEffects = deserializePotionEffects(MapUtils.getAsList(map, KEY_POTION_EFFECTS));

        Integer fireTicks = MapUtils.getOrNull(map, KEY_FIRE_TICKS);
        Integer ticksLived = MapUtils.getOrNull(map, KEY_TICKS_LIVED);
        Integer portalCooldown = MapUtils.getOrNull(map, KEY_PORTAL_COOLDOWN);
        Boolean persistent = MapUtils.getOrNull(map, KEY_PERSISTENT);
        Float fallDistance = MapUtils.getOrNull(map, KEY_FALL_DISTANCE);

        return new EntityBeanImpl(
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
        if (!(o instanceof EntityBeanImpl)) return false;
        EntityBeanImpl that = (EntityBeanImpl) o;
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
}
