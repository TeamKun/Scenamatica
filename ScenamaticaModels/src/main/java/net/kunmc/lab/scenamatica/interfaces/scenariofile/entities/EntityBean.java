package net.kunmc.lab.scenamatica.interfaces.scenariofile.entities;

import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * エンティティのインターフェースです。
 */
public interface EntityBean
{
    String KEY_LOCATION = "loc";
    String KEY_CUSTOM_NAME = "customName";
    String KEY_UUID = "uuid";
    String KEY_GLOWING = "glowing";
    String KEY_GRAVITY = "gravity";
    String KEY_TAGS = "tags";
    String KEY_LAST_DAMAGE = "lastDamage";
    String KEY_MAX_HEALTH = "maxHealth";
    String KEY_HEALTH = "health";
    String KEY_POTION_EFFECTS = "potion";
    String KEY_POTION_EFFECTS_AMBIENT = "ambient";
    String KEY_POTION_EFFECTS_AMPLIFIER = "amplifier";
    String KEY_POTION_EFFECTS_DURATION = "duration";
    String KEY_POTION_EFFECTS_TYPE = "type";
    String KEY_POTION_EFFECTS_SHOW_PARTICLES = "particle";
    String KEY_POTION_EFFECTS_SHOW_ICON = "icon";

    /**
     * このエンティティの位置を取得します。
     *
     * @return 位置
     */
    @Nullable
    Location getLocation();

    /**
     * このエンティティのカスタム名を取得します。
     *
     * @return カスタム名
     */
    @Nullable
    String getCustomName();

    /**
     * このエンティティのUUIDを取得します。
     *
     * @return UUID
     */
    @Nullable
    UUID getUuid();

    /**
     * このエンティティが光っているかどうかを取得します。
     *
     * @return 光っているかどうか
     */
    boolean isGlowing();

    /**
     * このエンティティが重力を受けるかどうかを取得します。
     *
     * @return 重力を受けるかどうか
     */
    boolean isGravity();

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
    @Nullable
    Integer getMaxHealth();

    /**
     * このエンティティの体力を取得します。
     *
     * @return 体力
     */
    @Nullable
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
    @Nullable
    DamageBean getLastDamageCause();
}
