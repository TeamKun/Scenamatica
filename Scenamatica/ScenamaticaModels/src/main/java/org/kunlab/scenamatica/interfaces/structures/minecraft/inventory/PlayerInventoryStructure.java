package org.kunlab.scenamatica.interfaces.structures.minecraft.inventory;

import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;

/**
 * プレイヤのインベントリの定義を表すインターフェースです。
 */
@TypeDoc(
        name = "PlayerInventory",
        description = "プレイヤのインベントリの情報を格納します。",
        mappingOf = PlayerInventory.class,
        properties = {
                @TypeProperty(
                        name = PlayerInventoryStructure.KEY_MAIN_INVENTORY,
                        description = "インベントリのアイテムです。",
                        type = ItemStackStructure[].class
                ),
                @TypeProperty(
                        name = PlayerInventoryStructure.KEY_MAIN_HAND,
                        description = "利き手のアイテムです。",
                        type = ItemStackStructure.class
                ),
                @TypeProperty(
                        name = PlayerInventoryStructure.KEY_OFF_HAND,
                        description = "オフハンドのアイテムです。",
                        type = ItemStackStructure.class
                ),
                @TypeProperty(
                        name = PlayerInventoryStructure.KEY_ARMOR_CONTENTS,
                        description = "プレイヤが着用している防具です。\n" +
                                "プレイヤの防具を昇順で以下のように割り当てます：\n" +
                                "\n" +
                                "1. ヘルメット\n" +
                                "2. チェストプレート\n" +
                                "3. レギンス\n" +
                                "4. ブーツ",
                        type = ItemStackStructure[].class,
                        admonitions = {
                                @Admonition(
                                        type= AdmonitionType.DANGER,
                                        content = "このプロパティの要素は, 必ず 4 つでなければいけません。\n" +
                                                "空を指定する場合は `null` で補完してください。\n" +
                                                "\n" +
                                                "例： armors: [null, DIAMOND_CHESTPLATE, GOLDEN_LEGGINGS, null]`"
                                )
                        }
                )
        }
)
public interface PlayerInventoryStructure extends InventoryStructure
{
    String KEY_MAIN_INVENTORY = "main";
    String KEY_MAIN_HAND = "mainHandItem";
    String KEY_OFF_HAND = "offHandItem";
    String KEY_ARMOR_CONTENTS = "armors";

    /**
     * メインハンドのアイテムを取得します。
     *
     * @return メインハンドのアイテム
     */
    @Nullable
    ItemStackStructure getMainHand();

    /**
     * オフハンドのアイテムを取得します。
     *
     * @return オフハンドのアイテム
     */
    @Nullable
    ItemStackStructure getOffHand();

    /**
     * 防具を取得します。
     * 配列のインデックスは以下の通りです。
     * <ul>
     *     <li>0 - ヘルメット</li>
     *     <li>1 - チェストプレート</li>
     *     <li>2 - レギンス</li>
     *     <li>3 - ブーツ</li>
     * </ul>
     *
     * @return 防具
     */
    @Nullable
    ItemStackStructure[] getArmorContents();

    @Override
    PlayerInventory create();
}
