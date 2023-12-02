package org.kunlab.scenamatica.interfaces.scenariofile.inventory;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * インベントリの構造体の既定のインターフェースです。
 */
public interface GenericInventoryStructure extends Structure
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
    Map<Integer, ItemStackStructure> getMainContents();
}
