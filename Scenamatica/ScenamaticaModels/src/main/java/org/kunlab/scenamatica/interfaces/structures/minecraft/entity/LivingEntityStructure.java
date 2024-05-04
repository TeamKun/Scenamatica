package org.kunlab.scenamatica.interfaces.structures.minecraft.entity;

import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.List;

public interface LivingEntityStructure extends EntityStructure
{
    String KEY_REMAINING_AIR = "remainAir";
    String KEY_MAX_AIR = "maxAir";
    String KEY_ARROW_COOLDOWN = "arrowCooldown";
    String KEY_ARROWS_IN_BODY = "arrowsInBody";
    String KEY_MAX_NO_DAMAGE_TICKS = "maxNoDamageTicks";
    String KEY_LAST_DAMAGE = "lastDamage";
    String KEY_NO_DAMAGE_TICKS = "noDamageTicks";
    String KEY_KILLER = "killer";
    String KEY_POTION_EFFECTS = "potions";
    String KEY_POTION_EFFECTS_AMBIENT = "ambient";
    String KEY_POTION_EFFECTS_AMPLIFIER = "amplifier";
    String KEY_POTION_EFFECTS_DURATION = "duration";
    String KEY_POTION_EFFECTS_TYPE = "type";
    String KEY_POTION_EFFECTS_SHOW_PARTICLES = "particle";
    String KEY_POTION_EFFECTS_SHOW_ICON = "icon";
    String KEY_REMOVE_WHEN_FAR_AWAY = "removeWhenFarAway";
    String KEY_CAN_PICKUP_ITEMS = "canPickupItems";
    String KEY_LEASHED = "leashed";
    String KEY_LEASH_HOLDER = "leashHolder";
    String KEY_GLIDING = "gliding";
    String KEY_SWIMMING = "swimming";
    String KEY_RIPTIDING = "riptiding";
    String KEY_SLEEPING = "sleeping";
    String KEY_AI = "ai";
    String KEY_COLLIDABLE = "collidable";
    String KEY_INVISIBLE = "invisible";
    // Paper
    String KEY_ARROWS_STUCK = "arrowsStuck";
    String KEY_SHIELD_BLOCKING_DELAY = "shieldBlockingDelay";
    String KEY_ACTIVE_ITEM = "activeItem";
    String KEY_ITEM_USE_REMAIN_TIME = "itemUseRemainTime";
    String KEY_HAND_RAISED_TIME = "handRaisedTime";
    String KEY_IS_HAND_RAISED = "isHandRaised";
    String KEY_HAND_RAISED = "handRaised";
    String KEY_JUMPING = "jumping";
    String KEY_HURT_DIRECTION = "hurtDirection";

    /**
     * このエンティティの残りの空気を取得します。
     *
     * @return 残り空気
     */
    Integer getRemainAir();

    /**
     * このエンティティの最大空気を取得します。
     *
     * @return 最大空気
     */
    Integer getMaxAir();

    /**
     * このエンティティの矢のクールダウンを取得します。
     *
     * @return 矢のクールダウン
     */
    Integer getArrowCooldown();

    /**
     * このエンティティの体に刺さっている矢の数を取得します。
     *
     * @return 体に刺さっている矢の数
     */
    Integer getArrowsInBody();

    /**
     * このエンティティの最大無敵時間を取得します。
     *
     * @return 最大無敵時間
     */
    Integer getMaxNoDamageTicks();

    /**
     * このエンティティの最後に受けたダメージを取得します。
     *
     * @return 最後に受けたダメージ
     */
    Double getLastDamage();

    /**
     * このエンティティの無敵時間を取得します。
     *
     * @return 無敵時間
     */
    Integer getNoDamageTicks();

    /**
     * このエンティティを殺したプレイヤーを取得します。
     *
     * @return 殺したプレイヤー
     */
    @NotNull
    PlayerSpecifier getKiller();

    /**
     * このエンティティに付与されているポーションエフェクトを取得します。
     *
     * @return ポーションエフェクト
     */
    @NotNull
    List<PotionEffect> getPotionEffects();

    /**
     * 遠く離れたときにこのエンティティを削除するかどうかを取得します。
     *
     * @return 遠く離れたときにこのエンティティを削除するかどうか
     */
    Boolean getRemoveWhenFarAway();

    /**
     * このエンティティがアイテムを拾えるかどうかを取得します。
     *
     * @return アイテムを拾えるかどうか
     */
    Boolean getCanPickupItems();

    /**
     * このエンティティがリードされているかどうかを取得します。
     *
     * @return リードされているかどうか
     */
    Boolean getLeashed();

    /**
     * このエンティティをリードしているエンティティを取得します。
     *
     * @return リードしているエンティティ
     */
    @NotNull
    EntitySpecifier<Entity> getLeashHolder();

    /**
     * このエンティティが滑空しているかどうかを取得します。
     *
     * @return 滑空しているかどうか
     */
    Boolean getGliding();

    /**
     * このエンティティが泳いでいるかどうかを取得します。
     *
     * @return 泳いでいるかどうか
     */
    Boolean getSwimming();

    /**
     * このエンティティがトライデントを投げているかどうかを取得します。
     *
     * @return トライデントを投げているかどうか
     */
    Boolean getRiptiding();

    /**
     * このエンティティが寝ているかどうかを取得します。
     *
     * @return 寝ているかどうか
     */
    Boolean getSleeping();

    /**
     * このエンティティがAIを持つかどうかを取得します。
     *
     * @return AIを持つかどうか
     */
    Boolean getAi();

    /**
     * このエンティティが干渉するかどうかを取得します。
     *
     * @return 干渉するかどうか
     */
    Boolean getCollidable();

    /**
     * このエンティティが透明かどうかを取得します。
     *
     * @return 透明かどうか
     */
    Boolean getInvisible();

    /**
     * このエンティティが持っている矢の数を取得します。
     *
     * @return 矢の数
     */
    Integer getArrowsStuck();

    /**
     * このエンティティが盾をブロックしている遅延時間を取得します。
     *
     * @return 盾をブロックしている遅延時間
     */
    Integer getShieldBlockingDelay();

    /**
     * このエンティティの有効なアイテムを取得します。
     *
     * @return 有効なアイテム
     */
    ItemStackStructure getActiveItem();

    /**
     * このエンティティのアイテム使用の残り時間を取得します。
     *
     * @return アイテム使用の残り時間
     */
    Integer getItemUseRemainTime();

    /**
     * このエンティティの手を上げている時間を取得します。
     *
     * @return 手を上げている時間
     */
    Integer getHandRaisedTime();

    /**
     * このエンティティが手を上げているかどうかを取得します。
     *
     * @return 手を上げているかどうか
     */
    Boolean getIsHandRaised();
}
