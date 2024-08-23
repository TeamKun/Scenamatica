package org.kunlab.scenamatica.interfaces.structures.minecraft.inventory;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * インベントリの構造体の基底のインターフェースです。
 */
@TypeDoc(
        name = "Inventory",
        description = "インベントリの情報を格納します。",
        mappingOf = Inventory.class,
        properties = {
                @TypeProperty(
                        name = InventoryStructure.KEY_SIZE,
                        description = "インベントリの大きさです。",
                        type = Integer.class,
                        min = 0,
                        admonitions = {
                                @Admonition(
                                        type = AdmonitionType.DANGER,
                                        content = "必ず 9 の倍数である必要があります。"
                                )
                        }
                ),
                @TypeProperty(
                        name = InventoryStructure.KEY_TITLE,
                        description = "インベントリのタイトルです。",
                        type = String.class,
                        admonitions = {
                                @Admonition(
                                        type = AdmonitionType.DANGER,
                                        content = "32 文字以内である必要があります。\n" +
                                                "これは, Minecraft の仕様によるものです。"
                                )
                        }
                ),
                @TypeProperty(
                        name = InventoryStructure.KEY_MAIN_CONTENTS,
                        description = "インベントリのメインコンテンツです。\n" +
                                "キーにはスロット番号を表す整数を, 値には ItemStackStructure を指定します。",
                        type = Map.class
                )
        }

)
@Category(
        id = "inventory_items",
        name = "インベントリ・アイテム",
        description = "インベントリ・アイテムに関する情報を格納します。"
)
public interface InventoryStructure extends Structure, Mapped, Creatable
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

    @Override
    Inventory create();

    /* @Overload */
    void applyTo(Inventory inventory);
    /* @Overload */
    boolean isAdequate(Inventory inventory, boolean strict);
    /* @Overload */
    default boolean isAdequate(Inventory inventory)
    {
        return this.isAdequate(inventory, false);
    }
}
