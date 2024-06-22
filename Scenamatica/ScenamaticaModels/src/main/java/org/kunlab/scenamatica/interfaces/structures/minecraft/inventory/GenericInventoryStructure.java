package org.kunlab.scenamatica.interfaces.structures.minecraft.inventory;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * インベントリの構造体の基底のインターフェースです。
 */
@TypeDoc(
        name = "Inventory",
        description = "インベントリの情報を格納します。",
        properties = {
                @TypeProperty(
                        name = GenericInventoryStructure.KEY_SIZE,
                        description = "インベントリの大きさです。",
                        type = Integer.class,
                        min = 0
                ),
                @TypeProperty(
                        name = GenericInventoryStructure.KEY_TITLE,
                        description = "インベントリのタイトルです。",
                        type = String.class
                ),
                @TypeProperty(
                        name = GenericInventoryStructure.KEY_MAIN_CONTENTS,
                        description = "インベントリのメインコンテンツです。",
                        type = Map.class
                )
        }

)
@Category(
        id = "inventory_items",
        name = "インベントリ・アイテム関連",
        description = "インベントリ・アイテムに関する情報を格納します。"
)
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
