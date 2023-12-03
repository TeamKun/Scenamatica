package org.kunlab.scenamatica.interfaces.scenariofile.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.List;
import java.util.UUID;

/**
 * エンティティの基底インターフェースです。
 */
public interface EntityStructure extends Structure
{
    String KEY_TYPE = "type";
    String KEY_LOCATION = "loc";
    String KEY_LOCATION_2 = "location";
    String KEY_VELOCITY = "velocity";
    String KEY_CUSTOM_NAME = "customName";
    String KEY_UUID = "uuid";
    String KEY_GLOWING = "glowing";
    String KEY_GRAVITY = "gravity";
    String KEY_SILENT = "silent";
    String KEY_CUSTOM_NAME_VISIBLE = "customNameVisible";
    String KEY_INVULNERABLE = "invulnerable";
    String KEY_TAGS = "tags";
    String KEY_LAST_DAMAGE = "lastDamage";
    String KEY_MAX_HEALTH = "maxHealth";
    String KEY_HEALTH = "health";
    String KEY_POTION_EFFECTS = "potions";
    String KEY_POTION_EFFECTS_AMBIENT = "ambient";
    String KEY_POTION_EFFECTS_AMPLIFIER = "amplifier";
    String KEY_POTION_EFFECTS_DURATION = "duration";
    String KEY_POTION_EFFECTS_TYPE = "type";
    String KEY_POTION_EFFECTS_SHOW_PARTICLES = "particle";
    String KEY_POTION_EFFECTS_SHOW_ICON = "icon";
    String KEY_FIRE_TICKS = "fireTicks";
    String KEY_TICKS_LIVED = "ticksLived";
    String KEY_PORTAL_COOLDOWN = "portalCooldown";
    String KEY_PERSISTENT = "persistent";
    String KEY_FALL_DISTANCE = "fallDistance";

    /**
     * エンティティの種類を取得します。
     *
     * @return 種類
     */
    EntityType getType();

    /**
     * このエンティティの位置を取得します。
     *
     * @return 位置
     */
    Location getLocation();

    /**
     * このエンティティの速度を取得します。
     *
     * @return 速度
     */
    Vector getVelocity();

    /**
     * このエンティティのカスタム名を取得します。
     *
     * @return カスタム名
     */
    String getCustomName();

    /**
     * このエンティティのUUIDを取得します。
     *
     * @return UUID
     */
    UUID getUuid();

    /**
     * このエンティティが光っているかどうかを取得します。
     *
     * @return 光っているかどうか
     */
    Boolean getGlowing();

    /**
     * このエンティティが重力を受けるかどうかを取得します。
     *
     * @return 重力を受けるかどうか
     */
    Boolean getGravity();

    /**
     * このエンティティが無音かどうかを取得します。
     *
     * @return 無音かどうか
     */
    Boolean getSilent();

    /**
     * このエンティティのカスタム名が表示されるかどうかを取得します。
     *
     * @return カスタム名が表示されるかどうか
     */
    Boolean getCustomNameVisible();

    /**
     * このエンティティが無敵かどうかを取得します。
     *
     * @return 無敵かどうか
     */
    Boolean getInvulnerable();

    /**
     * このエンティティに付与されているタグを取得します。
     *
     * @return タグ
     */
    @NotNull
    List<String> getTags();

    /**
     * このエンティティの最大体力を取得します。
     *
     * @return 最大体力
     */
    Integer getMaxHealth();

    /**
     * このエンティティの体力を取得します。
     *
     * @return 体力
     */
    Integer getHealth();

    /**
     * このエンティティに付与されているポーションエフェクトを取得します。
     *
     * @return ポーションエフェクト
     */
    @NotNull
    List<PotionEffect> getPotionEffects();

    /**
     * このエンティティの最後に与えられたダメージを取得します。
     *
     * @return ダメージ
     */
    DamageStructure getLastDamageCause();

    /**
     * このエンティティの燃焼時間を取得します。
     *
     * @return 燃焼時間
     */
    Integer getFireTicks();

    /**
     * このエンティティの生存時間を取得します。
     *
     * @return 生存時間
     */
    Integer getTicksLived();

    /**
     * このエンティティのポータルクールのダウンを取得します。
     *
     * @return ポータルクールダウン
     */
    Integer getPortalCooldown();

    /**
     * このエンティティが永続化されているかどうかを取得します。
     *
     * @return 永続化されているかどうか
     */
    Boolean getPersistent();

    /**
     * このエンティティの落下距離を取得します。
     *
     * @return 落下距離
     */
    Float getFallDistance();
}
