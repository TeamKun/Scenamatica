package net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory;

import java.io.Serializable;
import java.util.Map;

/**
 * インベントリの定義を表すインタフェースです。
 */
public interface InventoryBean extends Serializable
{
    String KEY_SIZE = "size";
    String KEY_TITLE = "title";
    String KEY_MAIN_CONTENTS = "items";

    /**
     * このインベントリのサイズを取得します。
     *
     * @return サイズ
     */
    int getSize();

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
    Map<Integer, ItemStackBean> getMainContents();
}
