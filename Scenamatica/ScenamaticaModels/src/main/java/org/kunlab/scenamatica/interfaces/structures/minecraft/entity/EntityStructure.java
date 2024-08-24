package org.kunlab.scenamatica.interfaces.structures.minecraft.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;

import java.util.List;
import java.util.UUID;

/**
 * エンティティの基底インターフェースです。
 */
@TypeDoc(
        name = "Entity",
        description = "エンティティの情報を格納します。",
        mappingOf = Entity.class,
        properties = {
                @TypeProperty(
                        name = EntityStructure.KEY_TYPE,
                        description = "エンティティの種類です。",
                        type = EntityType.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_LOCATION_2,
                        description = "エンティティの位置です。",
                        type = LocationStructure.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_VELOCITY,
                        description = "エンティティの速度です。",
                        type = LocationStructure.class  // 実質的には Vector なので LocationStructure とする
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_CUSTOM_NAME,
                        description = "エンティティのカスタム名です。",
                        type = String.class,
                        max = 16
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_UUID,
                        description = "エンティティの UUID です。",
                        type = UUID.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_GLOWING,
                        description = "エンティティが発光しているかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_GRAVITY,
                        description = "エンティティが重力を受けるかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_SILENT,
                        description = "エンティティが無音かどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_CUSTOM_NAME_VISIBLE,
                        description = "エンティティのカスタム名が表示されるかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_INVULNERABLE,
                        description = "エンティティが無敵かどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_TAGS,
                        description = "エンティティに付与されているタグです。",
                        type = String[].class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_LAST_DAMAGE_CAUSE,
                        description = "エンティティの最後に与えられたダメージです。",
                        type = DamageStructure.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_MAX_HEALTH,
                        description = "エンティティの最大体力です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_HEALTH,
                        description = "エンティティの体力です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_FIRE_TICKS,
                        description = "エンティティの燃焼時間です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_TICKS_LIVED,
                        description = "エンティティの生存時間です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_PORTAL_COOLDOWN,
                        description = "エンティティのポータルクールダウンです。",
                        type = int.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_PERSISTENT,
                        description = "エンティティが永続的かどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = EntityStructure.KEY_FALL_DISTANCE,
                        description = "エンティティの落下距離です。",
                        type = float.class
                )
        }
)
@Category(
        id = "entities",
        name = "エンティティ",
        description = "エンティティに関連する情報を格納します。"
)
public interface EntityStructure extends Structure, ProjectileSourceStructure, Mapped<Entity>
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
    String KEY_LAST_DAMAGE_CAUSE = "lastDamageCause";
    String KEY_MAX_HEALTH = "maxHealth";
    String KEY_HEALTH = "health";
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
    LocationStructure getLocation();

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

    @Override
    default boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof Entity;
    }

    /**
     * このエンティティを指定されたエンティティに適用します。
     *
     * @param entity        適用するエンティティ
     * @param applyLocation 位置情報を適用するかどうか
     */
    void applyTo(@NotNull Entity entity, boolean applyLocation);

    @Override
    default void applyTo(@NotNull Entity object)
    {
        this.applyTo(object, true);
    }
}
