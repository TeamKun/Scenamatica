package net.kunmc.lab.scenamatica.scenariofile.beans.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.DamageBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * エンティティの座標です。
     */
    @Nullable
    private final Location location;
    /**
     * エンティティのカスタム名です。
     */
    @Nullable
    private final String customName;
    /**
     * エンティティのUUIDです。
     */
    @Nullable
    private final UUID uuid;

    /**
     * エンティティが光っているかどうかです。
     */
    private final boolean glowing;

    /**
     * エンティティが重力を持っているかどうかです。
     */
    private final boolean gravity;

    /**
     * スコアボードのタグです。
     */
    @NotNull
    private final List<String> tags;
    /**
     * 最大体力です。
     */
    @Nullable
    private final Integer maxHealth;
    /**
     * 体力です。
     */
    @Nullable
    private final Integer health;
    /**
     * 最後のダメージの原因です。
     */
    @Nullable
    private final DamageBean lastDamageCause;

    /**
     * ポーションエフェクトのリストです。
     */
    @NotNull
    private final List<PotionEffect> potionEffects;

    public EntityBeanImpl()
    {
        this(
                null,
                null,
                null,
                false,
                true,
                Collections.emptyList(),
                null,
                null,
                null,
                Collections.emptyList()
        );
    }

    /**
     * エンティティ情報をMapにシリアライズします。
     *
     * @return エンティティ情報をシリアライズしたMap
     */
    public static Map<String, Object> serialize(@NotNull EntityBean entity)
    {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putLocationIfNotNull(map, KEY_LOCATION, entity.getLocation());
        MapUtils.putIfNotNull(map, KEY_CUSTOM_NAME, entity.getCustomName());
        if (entity.getUuid() != null)
            map.put(KEY_UUID, entity.getUuid().toString());
        if (entity.isGlowing())
            map.put(KEY_GLOWING, true);
        if (!entity.isGravity())
            map.put(KEY_GRAVITY, false);
        if (entity.getLastDamageCause() != null)
            map.put(EntityBean.KEY_LAST_DAMAGE, DamageBeanImpl.serialize(entity.getLastDamageCause()));

        MapUtils.putListIfNotEmpty(map, KEY_TAGS, entity.getTags());

        MapUtils.putIfNotNull(map, KEY_MAX_HEALTH, entity.getMaxHealth());
        MapUtils.putIfNotNull(map, KEY_HEALTH, entity.getHealth());

        if (!entity.getPotionEffects().isEmpty())
            map.put(KEY_POTION_EFFECTS, serializePotionEffects(entity.getPotionEffects()));


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
    public static EntityBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        Location loc = MapUtils.getAsLocationOrNull(map, KEY_LOCATION);
        String customName = MapUtils.getOrNull(map, KEY_CUSTOM_NAME);
        UUID uuid;
        if (map.containsKey(KEY_UUID))
            uuid = UUID.fromString((String) map.get(KEY_UUID));
        else
            uuid = null;

        boolean glowing = MapUtils.getOrDefault(map, KEY_GLOWING, false);
        boolean gravity = MapUtils.getOrDefault(map, KEY_GRAVITY, true);
        List<String> tags = MapUtils.getAsListOrEmpty(map, KEY_TAGS);

        DamageBean lastDamageCause = null;
        if (map.containsKey(KEY_LAST_DAMAGE))
            lastDamageCause = DamageBeanImpl.deserialize(MapUtils.checkAndCastMap(map.get(KEY_LAST_DAMAGE),
                    String.class, Object.class
            ));

        Integer maxHealth = MapUtils.getOrNull(map, KEY_MAX_HEALTH);
        Integer health = MapUtils.getOrNull(map, KEY_HEALTH);

        List<PotionEffect> potionEffects = new ArrayList<>();
        if (map.containsKey(KEY_POTION_EFFECTS))
            potionEffects = deserializePotionEffects(MapUtils.getAsList(map, KEY_POTION_EFFECTS));

        return new EntityBeanImpl(
                loc,
                customName,
                uuid,
                glowing,
                gravity,
                tags,
                maxHealth,
                health,
                lastDamageCause,
                potionEffects
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
        return Objects.hash(this.location, this.customName, this.uuid, this.glowing, this.gravity,
                this.tags, this.lastDamageCause, this.maxHealth, this.health
        );
    }
}
