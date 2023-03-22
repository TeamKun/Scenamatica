package net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    String getTitle();

    /**
     * このインベントリのメインコンテンツを取得します。
     *
     * @return メインコンテンツ
     */
    @NotNull
    Map<Integer, ItemStackBean> getMainContents();
}
