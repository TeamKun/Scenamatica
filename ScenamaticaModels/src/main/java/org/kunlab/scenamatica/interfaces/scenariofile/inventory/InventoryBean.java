package org.kunlab.scenamatica.interfaces.scenariofile.inventory;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Bean;

import java.util.Map;

/**
 * インベントリの定義を表すインタフェースです。
 */
public interface InventoryBean extends Bean
{
    String KEY_SIZE = "size";
    String KEY_TITLE = "title";
    String KEY_MAIN_CONTENTS = "items";

    /**
     * このインベントリのサイズを取得します。
     *
     * @return サイズ
     */
    Integer getSize();

    /**
     * このインベントリのタイトルを取得します。
     *
     * @return タイトル
     */
    String getTitle();

    /**
     * このインベントリのメインコンテンツを取得します。
     *
     * @return メインコンテンツ
     */
    @NotNull
    Map<Integer, ItemStackBean> getMainContents();

    /**
     * このインベントリのインスタンスを生成します。
     *
     * @return インベントリ
     */
    Inventory createInventory();
}
