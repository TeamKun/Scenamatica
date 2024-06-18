package org.kunlab.scenamatica.interfaces.structures.minecraft.inventory;

import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;

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
                        description = "防具です。",
                        type = ItemStackStructure[].class
                )
        }
)
public interface PlayerInventoryStructure extends GenericInventoryStructure, Mapped<PlayerInventory>, Creatable<PlayerInventory>
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
}
