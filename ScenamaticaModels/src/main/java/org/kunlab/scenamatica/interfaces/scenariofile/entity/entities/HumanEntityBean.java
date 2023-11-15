package org.kunlab.scenamatica.interfaces.scenariofile.entity.entities;

import org.bukkit.GameMode;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;

/**
 * 人型エンティティを表すインターフェースです。
 */
public interface HumanEntityBean extends EntityBean
{
    String KEY_INVENTORY = "inventory";
    String KEY_ENDER_CHEST = "enderChest";
    String KEY_MAIN_HAND = "mainHand";
    String KEY_GAMEMODE = "gamemode";
    String KEY_FOOD_LEVEL = "food";

    /**
     * 人型エンティティのインベントリを取得します。
     *
     * @return インベントリ
     */
    @Nullable
    PlayerInventoryBean getInventory();

    /**
     * 人型エンティティのエンダーチェストを取得します。
     *
     * @return エンダーチェスト
     */
    @Nullable
    InventoryBean getEnderChest();

    /**
     * 人型エンティティのメインハンドを取得します。
     * {@link MainHand#RIGHT} の場合は省略可能です。
     *
     * @return メインハンド
     */
    @NotNull
    MainHand getMainHand();

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
