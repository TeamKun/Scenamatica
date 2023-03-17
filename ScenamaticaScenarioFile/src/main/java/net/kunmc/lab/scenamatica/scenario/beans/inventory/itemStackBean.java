package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * インベントリのアイテムを表すクラスです。
 */
public class itemStackBean implements Serializable
{
    /**
     * アイテムの種類です。
     *
     * @see Material
     */
    @NotNull
    Material type;
    /**
     * アイテムの個数です。
     *
     * @see ItemStack#getAmount()
     * @see ItemStack#setAmount(int)
     */
    int amount;
    /**
     * アイテムの表示名です。
     *
     * @see ItemMeta#displayName()
     * @see ItemMeta#displayName()
     */
    @Nullable
    String displayName;

    /**
     * アイテムの翻訳名です。
     *
     * @see ItemMeta#getLocalizedName()
     * @see ItemMeta#setLocalizedName(String)
     */
    @Nullable
    String localizedName;

    /**
     * アイテムの説明（伝承）です。
     *
     * @see ItemMeta#lore()
     * @see ItemMeta#lore(java.util.List)
     */
    @NotNull
    List<String> lore;

    /**
     * アイテムのカスタムモデルデータです。
     *
     * @see ItemMeta#getCustomModelData()
     * @see ItemMeta#setCustomModelData(Integer)
     */
    @Nullable
    Integer customModelData;

    /**
     * アイテムのエンチャントです。
     *
     * @see ItemStack#getEnchantments()
     * @see ItemStack#addEnchantment(Enchantment, int)
     */
    @NotNull
    Map<Enchantment, Integer> enchantments;

    /**
     * アイテムのフラグです。
     *
     * @see ItemMeta#hasItemFlag(org.bukkit.inventory.ItemFlag)
     * @see ItemMeta#addItemFlags(org.bukkit.inventory.ItemFlag...)
     */
    @NotNull
    List<ItemFlag> itemFlags;

    /**
     * アイテムが非破壊かどうかです。
     *
     * @see ItemMeta#isUnbreakable()
     * @see ItemMeta#setUnbreakable(boolean)
     */
    boolean unbreakable;

    /**
     * アイテムの属性編集です。
     *
     * @see ItemMeta#getAttributeModifiers()
     * @see ItemMeta#setAttributeModifiers(Multimap)
     */
    @NotNull
    Map<Attribute, AttributeModifier> attributeModifiers;

    /**
     * アイテムをおける場所です。
     *
     * @see ItemMeta#getPlaceableKeys()
     * @see ItemMeta#setPlaceableKeys(Collection)
     */
    @NotNull
    List<String> placeableKeys;

    /**
     * 破壊可能キーです。
     *
     * @see ItemMeta#getDestroyableKeys()
     * @see ItemMeta#setDestroyableKeys(Collection)
     */
    @NotNull
    List<String> destroyableKeys;

    /**
     * アイテムの耐久値です。
     *
     * @see org.bukkit.inventory.meta.Damageable#getDamage()
     * @see org.bukkit.inventory.meta.Damageable#setDamage(int)
     */
    @Nullable
    Integer damage;
}
