package net.kunmc.lab.scenamatica.scenariofile.beans.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EntityBean
{
    public static final String KEY_LOCATION = "loc";
    public static final String KEY_CUSTOM_NAME = "customName";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_GLOWING = "glowing";
    public static final String KEY_GRAVITY = "gravity";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_LAST_DAMAGE = "lastDamage";
    public static final String KEY_MAX_HEALTH = "maxHealth";
    public static final String KEY_HEALTH = "health";

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
    @Nullable
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
    DamageBean lastDamageCause;

    public EntityBean()
    {
        this(
                null,
                null,
                null,
                false,
                true,
                null,
                null,
                null,
                null
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
        MapUtils.putIfNotNull(map, KEY_UUID, String.valueOf(entity.getUuid()));
        MapUtils.putIfNotNull(map, KEY_CUSTOM_NAME, entity.getCustomName());
        if (entity.isGlowing())
            map.put(KEY_GLOWING, true);
        if (!entity.isGravity())
            map.put(KEY_GRAVITY, false);
        if (entity.getLastDamageCause() != null)
            map.put(EntityBean.KEY_LAST_DAMAGE, DamageBean.serialize(entity.getLastDamageCause()));

        MapUtils.putListIfNotEmpty(map, KEY_TAGS, entity.getTags());

        MapUtils.putIfNotNull(map, KEY_MAX_HEALTH, entity.getMaxHealth());
        MapUtils.putIfNotNull(map, KEY_HEALTH, entity.getHealth());

        return map;
    }

    /**
     * Mapが正しいエンティティ情報かどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException Mapが正しいエンティティ情報ではない場合
     */
    public static void validateMap(@NotNull Map<String, Object> map)
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
    }

    /**
     * Mapからエンティティ情報をデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズしたエンティティ情報
     */
    public static EntityBean deserialize(@NotNull Map<String, Object> map)
    {
        validateMap(map);

        Location loc = MapUtils.getAsLocationOrNull(map, KEY_LOCATION);
        String customName = MapUtils.getOrNull(map, KEY_CUSTOM_NAME);
        UUID uuid;
        if (map.containsKey(KEY_UUID))
            uuid = UUID.fromString((String) map.get(KEY_UUID));
        else
            uuid = null;

        boolean glowing = MapUtils.getOrDefault(map, KEY_GLOWING, false);
        boolean gravity = MapUtils.getOrDefault(map, KEY_GRAVITY, true);
        List<String> tags = MapUtils.getAsListOrNull(map, KEY_TAGS);

        DamageBean lastDamageCause = null;
        if (map.containsKey(KEY_LAST_DAMAGE))
            lastDamageCause = DamageBean.deserialize(MapUtils.checkAndCastMap(map.get(KEY_LAST_DAMAGE),
                            String.class, Object.class
                    )
            );

        Integer maxHealth = MapUtils.getOrNull(map, KEY_MAX_HEALTH);
        Integer health = MapUtils.getOrNull(map, KEY_HEALTH);

        return new EntityBean(
                loc,
                customName,
                uuid,
                glowing,
                gravity,
                tags,
                maxHealth,
                health,
                lastDamageCause
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof EntityBean)) return false;
        EntityBean that = (EntityBean) o;
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
