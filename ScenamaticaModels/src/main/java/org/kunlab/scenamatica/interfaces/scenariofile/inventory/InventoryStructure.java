package org.kunlab.scenamatica.interfaces.scenariofile.inventory;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * インベントリの定義を表すインタフェースです。
 */
public interface InventoryStructure<T extends Inventory> extends Structure, Mapped<T>, Creatable<T>
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
