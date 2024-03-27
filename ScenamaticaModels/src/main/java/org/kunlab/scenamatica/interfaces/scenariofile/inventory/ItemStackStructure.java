package org.kunlab.scenamatica.interfaces.scenariofile.inventory;

import com.destroystokyo.paper.Namespaced;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.List;
import java.util.Map;

/**
 * インベントリのアイテムを表すインタフェースです。
 */
public interface ItemStackStructure extends Structure, Mapped<ItemStack>, Creatable<ItemStack>
{
    String KEY_TYPE = "type";
    String KEY_AMOUNT = "amount";
    String KEY_DISPLAY_NAME = "name";
    String KEY_LOCALIZED_NAME = "localizedName";
    String KEY_LORE = "lores";
    String KEY_CUSTOM_MODEL_DATA = "customModel";
    String KEY_ENCHANTMENTS = "enchants";
    String KEY_ITEM_FLAGS = "flags";
    String KEY_UNBREAKABLE = "unbreakable";
    String KEY_PLACEABLES = "placeables";
    String KEY_DESTROYABLES = "destroyables";
    String KEY_DAMAGE = "damage";
    String KEY_ATTRIBUTE_MODIFIERS = "attributes";
    String KEY_ATTRIBUTE_MODIFIER_NAME = "name";
    String KEY_ATTRIBUTE_MODIFIER_UUID = "uuid";
    String KEY_ATTRIBUTE_MODIFIER_AMOUNT = "amount";
    String KEY_ATTRIBUTE_MODIFIER_OPERATION = "operation";
    String KEY_ATTRIBUTE_MODIFIER_SLOT = "slot";

    /**
     * このアイテムの種類を取得します。
     *
     * @return アイテムの種類
     */
    Material getType();

    /**
     * このアイテムの個数を取得します。
     * <p>
     * 1 の場合は, シリアライズ時に省略されます。
     *
     * @return アイテムの個数
     */
    Integer getAmount();

    /**
     * このアイテムの表示名を取得します。
     *
     * @return アイテムの表示名
     */
    String getDisplayName();

    /**
     * このアイテムのローカライズされた名前を取得します。
     *
     * @return アイテムのローカライズされた名前
     */
    String getLocalizedName();

    /**
     * このアイテムの説明文を取得します。
     *
     * @return アイテムの説明文
     */
    @NotNull
    List<String> getLore();

    /**
     * このアイテムに付与されているエンチャントを取得します。
     *
     * @return アイテムに付与されているエンチャント
     */
    @NotNull
    Map<Enchantment, Integer> getEnchantments();

    /**
     * このアイテムに付与されているアイテムフラグを取得します。
     *
     * @return アイテムに付与されているアイテムフラグ
     */
    @NotNull
    List<ItemFlag> getItemFlags();

    /**
     * このアイテムが耐久無限かどうかを取得します。
     *
     * @return 耐久無限かどうか
     */
    Boolean getUnbreakable();

    /**
     * このアイテムに付与されている属性修飾を取得します。
     *
     * @return アイテムに付与されている属性修飾
     */
    @NotNull
    Map<Attribute, List<AttributeModifier>> getAttributeModifiers();

    /**
     * このアイテムが設置可能なブロックを取得します。
     *
     * @return 設置可能なブロック
     */
    @NotNull
    List<Namespaced> getPlaceableKeys();

    /**
     * このアイテムが破壊可能なブロックを取得します.
     *
     * @return 破壊可能なブロック
     */
    @NotNull
    List<Namespaced> getDestroyableKeys();

    /**
     * このアイテムのダメージ値を取得します.
     *
     * @return ダメージ値
     */
    Integer getDamage();
}
