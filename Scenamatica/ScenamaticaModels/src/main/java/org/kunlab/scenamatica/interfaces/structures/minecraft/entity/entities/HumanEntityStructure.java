package org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.LivingEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.util.Map;

/**
 * 人型エンティティを表す基底インターフェースです。
 */
@TypeDoc(
        name = "HumanEntity",
        description = "人型エンティティの情報を格納します。",
        mappingOf = org.bukkit.entity.HumanEntity.class,
        properties = {
                @TypeProperty(
                        name = HumanEntityStructure.KEY_INVENTORY,
                        description = "人型エンティティのインベントリです。",
                        type = PlayerInventoryStructure.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_ENDER_CHEST,
                        description = "人型エンティティのエンダーチェストです。",
                        type = InventoryStructure.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_MAIN_HAND,
                        description = "人型エンティティのメインハンドです。",
                        type = MainHand.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_COOLDOWN,
                        description = "人型エンティティの存在に対するクールダウンです。キーには素材（Material）を, 値には Tick 数を格納します。",
                        type = Map.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_SLEEPING,
                        description = "人型エンティティが寝ているかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_BED_SPAWN_LOCATION,
                        description = "人形エンティティのベッドのスポーン地点です。",
                        type = Location.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_BLOCKING,
                        description = "人型エンティティがブロックしているかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_GAMEMODE,
                        description = "人型エンティティのゲームモードです。",
                        type = GameMode.class
                ),
                @TypeProperty(
                        name = HumanEntityStructure.KEY_FOOD_LEVEL,
                        description = "人型エンティティの食料レベルです。",
                        type = int.class
                )
        }

)
public interface HumanEntityStructure extends LivingEntityStructure
{
    String KEY_INVENTORY = "inventory";
    String KEY_ENDER_CHEST = "enderChest";
    String KEY_MAIN_HAND = "mainHand";
    String KEY_COOLDOWN = "cooldown";
    String KEY_SLEEP_TICKS = "sleepTicks";
    String KEY_BED_SPAWN_LOCATION = "bedSpawnLocation";
    String KEY_BLOCKING = "blocking";
    String KEY_GAMEMODE = "gamemode";
    String KEY_FOOD_LEVEL = "food";

    /**
     * 人型エンティティのインベントリを取得します。
     *
     * @return インベントリ
     */
    @Nullable
    PlayerInventoryStructure getInventory();

    /**
     * 人型エンティティのエンダーチェストを取得します。
     *
     * @return エンダーチェスト
     */
    @Nullable
    InventoryStructure getEnderChest();

    /**
     * 人型エンティティのメインハンドを取得します。
     * {@link MainHand#RIGHT} の場合は省略可能です。
     *
     * @return メインハンド
     */
    @NotNull
    MainHand getMainHand();

    /**
     * 人形エンティティの素材に対するクールダウンの情報を取得します。
     *
     * @return クールダウンの情報
     */
    Map<Material, Integer> getCooldown();

    /**
     * 人型エンティティが寝ている際の経過 Tick 数を取得します。
     *
     * @return 経過 Tick 数
     */
    Integer getSleepTicks();

    /**
     * 人型エンティティがブロックしているかどうかを取得します。
     *
     * @return ブロックしているかどうか
     */
    Boolean getBlocking();

    /**
     * ベッドのスポーン地点を取得します。
     *
     * @return ベッドのスポーン地点
     */
    LocationStructure getBedSpawnLocation();

    /**
     * 人型エンティティのゲームモードを取得します。
     * {@link GameMode#SURVIVAL} の場合は省略可能です。
     *
     * @return ゲームモード
     */
    @NotNull
    GameMode getGamemode();

    /**
     * 人型エンティティの食料レベルを取得します。
     *
     * @return 食料レベル
     */
    @Nullable
    Integer getFoodLevel();
}
