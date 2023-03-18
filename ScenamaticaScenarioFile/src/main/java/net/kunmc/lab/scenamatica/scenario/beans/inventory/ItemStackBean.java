package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.commons.utils.NamespaceUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * インベントリのアイテムを表すクラスです。
 */
@Value
public class ItemStackBean implements Serializable
{
    private static final String KEY_TYPE = "type";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_DISPLAY_NAME = "name";
    private static final String KEY_LOCALIZED_NAME = "localizedName";
    private static final String KEY_LORE = "lores";
    private static final String KEY_CUSTOM_MODEL_DATA = "customModel";
    private static final String KEY_ENCHANTMENTS = "enchants";
    private static final String KEY_ITEM_FLAGS = "flags";
    private static final String KEY_UNBREAKABLE = "unbreakable";
    private static final String KEY_PLACEABLES = "placeables";
    private static final String KEY_DESTROYABLES = "destroyables";
    private static final String KEY_DAMAGE = "damage";

    private static final String KEY_ATTRIBUTE_MODIFIERS = "attributes";
    private static final String KEY_ATTRIBUTE_MODIFIER_AMOUNT = "amount";
    private static final String KEY_ATTRIBUTE_MODIFIER_OPERATION = "operation";
    private static final String KEY_ATTRIBUTE_MODIFIER_SLOT = "slot";

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
    @Nullable
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
    List<Namespaced> placeableKeys;

    /**
     * 破壊可能キーです。
     *
     * @see ItemMeta#getDestroyableKeys()
     * @see ItemMeta#setDestroyableKeys(Collection)
     */
    @NotNull
    List<Namespaced> destroyableKeys;

    /**
     * アイテムの耐久値です。
     *
     * @see org.bukkit.inventory.meta.Damageable#getDamage()
     * @see org.bukkit.inventory.meta.Damageable#setDamage(int)
     */
    @Nullable
    Integer damage;

    private static Map<String, Object> serializeEnchantments(ItemStackBean bean)
    {
        Map<String, Object> result = new HashMap<>();
        if (!bean.enchantments.isEmpty())
        {
            Map<String, Object> enchantments = new HashMap<>();
            for (Map.Entry<Enchantment, Integer> entry : bean.enchantments.entrySet())
            {
                String key = NamespaceUtils.toString(entry.getKey().getKey());
                Integer value = entry.getValue();
                enchantments.put(key, value);
            }
            result.put(KEY_ENCHANTMENTS, enchantments);
        }

        return result;
    }

    private static void validateEnchantments(Map<String, Object> map)
    {
        if (map.containsKey(KEY_ENCHANTMENTS))
        {
            Map<String, Object> enchtansMap = MapUtils.checkAndCastMap(
                    map.get(KEY_ENCHANTMENTS),
                    String.class, Object.class
            );

            for (Map.Entry<String, Object> entry : enchtansMap.entrySet())
            {
                if (Enchantment.getByKey(NamespaceUtils.fromString(entry.getKey())) == null)
                    throw new IllegalArgumentException("Invalid enchantment key: " + entry.getKey());
                if (!(entry.getValue() instanceof Integer))
                    throw new IllegalArgumentException("Invalid enchantment value: " + entry.getValue());
            }
        }
    }

    private static Map<Enchantment, Integer> deserializeEnchantments(Map<String, Object> map)
    {
        Map<Enchantment, Integer> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : MapUtils.getOrDefault(map, KEY_ENCHANTMENTS,
                        Collections.<String, Object>emptyMap()).entrySet())
        {
            Enchantment enchantment = Enchantment.getByKey(NamespaceUtils.fromString(entry.getKey()));
            int level = (int) entry.getValue();
            result.put(enchantment, level);
        }

        return result;
    }

    private static Map<String, Object> serializeAttributeModifiers(ItemStackBean bean)
    {
        Map<String, Object> result = new HashMap<>();
        if (!bean.attributeModifiers.isEmpty())
        {
            Map<String, Object> attributeModifiers = new HashMap<>();
            for (Map.Entry<Attribute, AttributeModifier> entry : bean.attributeModifiers.entrySet())
            {
                Map<String, Object> attrs =  new HashMap<>();
                AttributeModifier value = entry.getValue();

                attributeModifiers.put(KEY_ATTRIBUTE_MODIFIER_AMOUNT, value.getAmount());
                attributeModifiers.put(KEY_ATTRIBUTE_MODIFIER_OPERATION, value.getOperation().name());
                MapUtils.putAsStrIfNotNull(attributeModifiers, KEY_ATTRIBUTE_MODIFIER_SLOT, value.getSlot());

                String key = NamespaceUtils.toString(entry.getKey().getKey())
                        .toLowerCase(Locale.ROOT);

                attributeModifiers.put(key, attrs);
            }
            result.put(KEY_ATTRIBUTE_MODIFIERS, attributeModifiers);
        }

        return result;
    }

    private static void validateAttributeModifiers(Map<String, Object> map)
    {
        if (map.containsKey(KEY_ATTRIBUTE_MODIFIERS))
        {
            Map<String, Object> attributesMap = MapUtils.checkAndCastMap(
                    map.get(KEY_ATTRIBUTE_MODIFIERS),
                    String.class, Object.class
            );

            for (Map.Entry<String, Object> entry : attributesMap.entrySet())
            {
                String name = entry.getKey();
                Map<String, Object> valuesMap = MapUtils.checkAndCastMap(
                        entry.getValue(),
                        String.class, Object.class
                );

                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_AMOUNT, Number.class);   // double
                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_OPERATION, String.class);
                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_SLOT, String.class);

                Attribute.valueOf(name.toUpperCase(Locale.ROOT)
                        .replace(".", "_")
                        .replace("GENERIC_", ""));  // GENERIC_ は省略できる
            }
        }
    }

    private static Map<Attribute, AttributeModifier> deserializeAttributeModifiers(Map<String, Object> map)
    {
        Map<Attribute, AttributeModifier> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : MapUtils.getOrDefault(map, KEY_ATTRIBUTE_MODIFIERS, Collections.<String, Object>emptyMap()).entrySet())
        {
            String attrName = entry.getKey().toUpperCase(Locale.ROOT)
                    .replace(".", "_");
            Map<String, Object> valuesMap = MapUtils.checkAndCastMap(
                    entry.getValue(),
                    String.class, Object.class
            );

            double attrAmount = MapUtils.getOrDefault(valuesMap, KEY_ATTRIBUTE_MODIFIER_AMOUNT, 0.0);
            AttributeModifier.Operation attrOperation =
                    MapUtils.getAsEnum(valuesMap, KEY_ATTRIBUTE_MODIFIER_OPERATION, AttributeModifier.Operation.class);
            EquipmentSlot slot = MapUtils.getAsEnum(valuesMap, KEY_ATTRIBUTE_MODIFIER_SLOT, EquipmentSlot.class);

            AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    attrName,
                    attrAmount,
                    attrOperation,
                    slot
            );

            result.put(getAttributeFromString(attrName), modifier);
        }

        return result;
    }

    public static Map<String, Object> serialize(ItemStackBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        // 必須項目
        map.put("type", bean.type.name());

        // オプション項目
        if (bean.amount != 1)  // 1個の場合は省略できる。
            map.put("amount", bean.amount);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DISPLAY_NAME, bean.displayName);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_LOCALIZED_NAME, bean.localizedName);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_CUSTOM_MODEL_DATA, bean.customModelData);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_UNBREAKABLE, bean.unbreakable);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DAMAGE, bean.damage);

        MapUtils.putListIfNotEmpty(map, KEY_LORE, bean.lore);
        MapUtils.putPrimitiveOrStrIfNotEmpty(map, KEY_PLACEABLES, bean.placeableKeys);
        MapUtils.putPrimitiveOrStrIfNotEmpty(map, KEY_DESTROYABLES, bean.destroyableKeys);
        MapUtils.putPrimitiveOrStrIfNotEmpty(map, KEY_ITEM_FLAGS, bean.itemFlags);

        MapUtils.putIfNotNull(map, KEY_ENCHANTMENTS, serializeEnchantments(bean));
        MapUtils.putIfNotNull(map, KEY_ATTRIBUTE_MODIFIER_SLOT, serializeAttributeModifiers(bean));

        return map;
    }

    public static void validateMap(Map<String, Object> map)
    {
        MapUtils.checkEnumName(map, KEY_TYPE, Material.class);

        MapUtils.checkTypeIfContains(map, KEY_AMOUNT, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_DISPLAY_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_LOCALIZED_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_LORE, List.class);
        MapUtils.checkTypeIfContains(map, KEY_CUSTOM_MODEL_DATA, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_ITEM_FLAGS, List.class);
        MapUtils.checkTypeIfContains(map, KEY_UNBREAKABLE, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_DAMAGE, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_PLACEABLES, List.class);
        MapUtils.checkTypeIfContains(map, KEY_DESTROYABLES, List.class);

        validateEnchantments(map);
        validateAttributeModifiers(map);
    }

    private static Attribute getAttributeFromString(String name)
    {
        String maybeGeneric = name.toUpperCase(Locale.ROOT);

        try
        {
            return Attribute.valueOf(maybeGeneric);
        }
        catch (IllegalArgumentException e)
        {
            return Attribute.valueOf("GENERIC_" + maybeGeneric);
        }
    }


    public static ItemStackBean deserialize(@NotNull Map<String, Object> map)
    {
        validateMap(map);

        // 必須項目
        Material type = Material.valueOf((String) map.get(KEY_TYPE));

        // オプション項目
        int amount = MapUtils.getOrDefault(map, KEY_AMOUNT, 1);
        String name = MapUtils.getOrNull(map, KEY_DISPLAY_NAME);
        String localizedName = MapUtils.getOrNull(map, KEY_LOCALIZED_NAME);
        List<String> lore = MapUtils.getOrDefault(map, KEY_LORE, Collections.emptyList());
        Integer customModelData = MapUtils.getOrNull(map, KEY_CUSTOM_MODEL_DATA);
        List<ItemFlag> flags = MapUtils.getAsEnumOrEmptyList(map, KEY_ITEM_FLAGS, ItemFlag.class);
        boolean unbreakable = Boolean.parseBoolean(MapUtils.getOrDefault(map, KEY_UNBREAKABLE, "false"));
        Integer damage = MapUtils.getOrNull(map, KEY_DAMAGE);

        List<Namespaced> placeableKeys = new ArrayList<>();
        for (String key : MapUtils.getOrDefault(map, KEY_PLACEABLES, Collections.<String>emptyList()))
            placeableKeys.add(NamespaceUtils.fromString(key));

        List<Namespaced> destroyableKeys = new ArrayList<>();
        for (String key : MapUtils.getOrDefault(map, KEY_DESTROYABLES, Collections.<String>emptyList()))
            destroyableKeys.add(NamespaceUtils.fromString(key));

        Map<Enchantment, Integer> enchantments = deserializeEnchantments(map);
        Map<Attribute, AttributeModifier> attributeModifiers = deserializeAttributeModifiers(map);

        return new ItemStackBean(type,
                amount,
                name,
                localizedName,
                lore,
                customModelData,
                enchantments,
                flags,
                unbreakable,
                attributeModifiers,
                placeableKeys,
                destroyableKeys,
                damage
        );
    }
}
