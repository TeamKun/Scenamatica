package net.kunmc.lab.scenamatica.scenariofile.beans.inventory;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
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
import org.bukkit.inventory.meta.Damageable;
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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * インベントリのアイテムを表すクラスです。
 */
@Value
@AllArgsConstructor
public class ItemStackBean implements Serializable
{
    public static final String KEY_TYPE = "type";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_DISPLAY_NAME = "name";
    public static final String KEY_LOCALIZED_NAME = "localizedName";
    public static final String KEY_LORE = "lores";
    public static final String KEY_CUSTOM_MODEL_DATA = "customModel";
    public static final String KEY_ENCHANTMENTS = "enchants";
    public static final String KEY_ITEM_FLAGS = "flags";
    public static final String KEY_UNBREAKABLE = "unbreakable";
    public static final String KEY_PLACEABLES = "placeables";
    public static final String KEY_DESTROYABLES = "destroyables";
    public static final String KEY_DAMAGE = "damage";

    public static final String KEY_ATTRIBUTE_MODIFIERS = "attributes";
    public static final String KEY_ATTRIBUTE_MODIFIER_NAME = "name";
    public static final String KEY_ATTRIBUTE_MODIFIER_AMOUNT = "amount";
    public static final String KEY_ATTRIBUTE_MODIFIER_OPERATION = "operation";
    public static final String KEY_ATTRIBUTE_MODIFIER_SLOT = "slot";

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
    Map<Attribute, List<AttributeModifier>> attributeModifiers;

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
            for (Map.Entry<Enchantment, Integer> entry : bean.enchantments.entrySet())
            {
                String key = NamespaceUtils.toString(entry.getKey().getKey());
                Integer value = entry.getValue();
                result.put(key, value);
            }

        return result;
    }

    @SuppressWarnings("deprecation")
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
                if (Enchantment.getByKey(NamespaceUtils.fromString(entry.getKey())) == null
                    && Enchantment.getByName(entry.getKey()) == null)
                    throw new IllegalArgumentException("Invalid enchantment key: " + entry.getKey());
                if (!(entry.getValue() instanceof Integer))
                    throw new IllegalArgumentException("Invalid enchantment value: " + entry.getValue());
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static Map<Enchantment, Integer> deserializeEnchantments(Map<String, Object> map)
    {
        Map<Enchantment, Integer> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : MapUtils.getOrDefault(map, KEY_ENCHANTMENTS,
                        Collections.<String, Object>emptyMap()).entrySet())
        {
            Enchantment enchantment = Enchantment.getByKey(NamespaceUtils.fromString(entry.getKey()));
            if (enchantment == null)
                enchantment = Enchantment.getByName(entry.getKey());

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
            for (Map.Entry<Attribute, List<AttributeModifier>> entry : bean.attributeModifiers.entrySet())
            {
                List<Map<String, Object>> list = new ArrayList<>();
                for (AttributeModifier modifier : entry.getValue())
                {
                    Map<String, Object> attrs = new HashMap<>();

                    attrs.put(KEY_ATTRIBUTE_MODIFIER_NAME, modifier.getName());
                    attrs.put(KEY_ATTRIBUTE_MODIFIER_AMOUNT, modifier.getAmount());
                    attrs.put(KEY_ATTRIBUTE_MODIFIER_OPERATION, modifier.getOperation().name());
                    MapUtils.putAsStrIfNotNull(attrs, KEY_ATTRIBUTE_MODIFIER_SLOT, modifier.getSlot());

                    list.add(attrs);
                }

                String key = NamespaceUtils.toString(entry.getKey().getKey())
                        .replace("generic.", "")
                        .toLowerCase(Locale.ROOT);

                result.put(key, list);
            }
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    private static void validateAttributeModifiersMap(Map<String, Object> map)
    {
        if (!map.containsKey(KEY_ATTRIBUTE_MODIFIERS))
            return;

        Map<String, List> attributesMap = MapUtils.checkAndCastMap(
                map.get(KEY_ATTRIBUTE_MODIFIERS),
                String.class, List.class
        );

        for (Map.Entry<String, List> entry : attributesMap.entrySet())
        {
            String name = entry.getKey();
            for (Object obj : entry.getValue())
            {
                Map<String, Object> valuesMap = MapUtils.checkAndCastMap(
                        obj,
                        String.class, Object.class
                );

                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_AMOUNT, Number.class);   // double
                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_OPERATION, String.class);
                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_SLOT, String.class);

                ItemStackBean.getAttributeFromString(name);
            }
        }

    }

    private static Map<Attribute, List<AttributeModifier>> deserializeAttributeModifiers(Map<String, Object> map)
    {
        Map<Attribute, List<AttributeModifier>> result = new HashMap<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry :
                MapUtils.getOrDefault(map, KEY_ATTRIBUTE_MODIFIERS, Collections.<String, List<Map<String, Object>>>emptyMap()).entrySet())
        {
            String attrName = entry.getKey().toUpperCase(Locale.ROOT)
                    .replace(".", "_");

            List<AttributeModifier> modifiers = new ArrayList<>();
            for (Map<String, Object> attrs : entry.getValue())
            {
                double attrAmount = MapUtils.getOrDefault(attrs, KEY_ATTRIBUTE_MODIFIER_AMOUNT, 0.0);
                AttributeModifier.Operation attrOperation =
                        MapUtils.getAsEnum(attrs, KEY_ATTRIBUTE_MODIFIER_OPERATION, AttributeModifier.Operation.class);
                EquipmentSlot slot = MapUtils.getAsEnumOrNull(attrs, KEY_ATTRIBUTE_MODIFIER_SLOT, EquipmentSlot.class);

                modifiers.add(new AttributeModifier(
                        UUID.randomUUID(),
                        attrs.get(KEY_ATTRIBUTE_MODIFIER_NAME).toString(),
                        attrAmount,
                        attrOperation,
                        slot
                ));
            }

            result.put(getAttributeFromString(attrName), modifiers);
        }

        return result;
    }

    public ItemStackBean(Material material)
    {
        this(
                material,
                1,
                null,
                null,
                Collections.emptyList(),
                null,
                Collections.emptyMap(),
                Collections.emptyList(),
                false,
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
    }

    public ItemStackBean(Material material, int amount)
    {
        this(
                material,
                amount,
                null,
                null,
                Collections.emptyList(),
                null,
                Collections.emptyMap(),
                Collections.emptyList(),
                false,
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
    }

    /**
     * アイテムスタックの情報をMapにシリアライズします。
     *
     * @param bean アイテムスタックの情報
     * @return シリアライズされたMap
     */
    public static Map<String, Object> serialize(ItemStackBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        // 必須項目
        map.put(KEY_TYPE, bean.type.name());

        // オプション項目
        if (bean.amount != 1)  // 1個の場合は省略できる。
            map.put(KEY_AMOUNT, bean.amount);
        if (bean.unbreakable)
            map.put(KEY_UNBREAKABLE, true);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DISPLAY_NAME, bean.displayName);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_LOCALIZED_NAME, bean.localizedName);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_CUSTOM_MODEL_DATA, bean.customModelData);
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DAMAGE, bean.damage);

        MapUtils.putListIfNotEmpty(map, KEY_LORE, bean.lore);
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_PLACEABLES, bean.placeableKeys);
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_DESTROYABLES, bean.destroyableKeys);
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_ITEM_FLAGS, bean.itemFlags);

        MapUtils.putMapIfNotEmpty(map, KEY_ENCHANTMENTS, serializeEnchantments(bean));
        MapUtils.putMapIfNotEmpty(map, KEY_ATTRIBUTE_MODIFIERS, serializeAttributeModifiers(bean));

        return map;
    }

    /**
     * Mapがアイテムスタックの情報を表すMapかどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException 必須項目が含まれていない場合か, 型が不正な場合
     */
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
        validateAttributeModifiersMap(map);
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
            return Attribute.valueOf("GENERIC_" + maybeGeneric);  // GENERIC_ は省略できる
        }
    }

    /**
     * Mapからアイテムスタックの情報をデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズされたアイテムスタックの情報
     */
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
        boolean unbreakable = MapUtils.getOrDefault(map, KEY_UNBREAKABLE, false);
        Integer damage = MapUtils.getOrNull(map, KEY_DAMAGE);

        List<Namespaced> placeableKeys = new ArrayList<>();
        for (String key : MapUtils.getOrDefault(map, KEY_PLACEABLES, Collections.<String>emptyList()))
            placeableKeys.add(NamespaceUtils.fromString(key));

        List<Namespaced> destroyableKeys = new ArrayList<>();
        for (String key : MapUtils.getOrDefault(map, KEY_DESTROYABLES, Collections.<String>emptyList()))
            destroyableKeys.add(NamespaceUtils.fromString(key));

        Map<Enchantment, Integer> enchantments = deserializeEnchantments(map);
        Map<Attribute, List<AttributeModifier>> attributeModifiers = deserializeAttributeModifiers(map);

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

    @Override
    public String toString()
    {
        return "ItemStackBean{" +
                "type=" + this.type +
                ", amount=" + this.amount +
                ", displayName='" + this.displayName + '\'' +
                ", localizedName='" + this.localizedName + '\'' +
                ", lore=" + this.lore +
                ", customModelData=" + this.customModelData +
                ", enchantments=" + this.enchantments +
                ", itemFlags=" + this.itemFlags +
                ", unbreakable=" + this.unbreakable +
                ", attributeModifiers=" + this.attributeModifiers +
                ", placeableKeys=" + this.placeableKeys +
                ", destroyableKeys=" + this.destroyableKeys +
                ", damage=" + this.damage +
                '}';
    }

    private static boolean isAttributeModifiersEquals(Map<Attribute, ? extends List<AttributeModifier>> thisModifiersMap, Map<Attribute, ? extends List<AttributeModifier>> thatModifiersMap)
    {
        for (Attribute attribute : thisModifiersMap.keySet())
        {
            if (!thatModifiersMap.containsKey(attribute))
                return true;

            List<AttributeModifier> thisModifiers = thisModifiersMap.get(attribute);
            List<AttributeModifier> thatModifiers = thatModifiersMap.get(attribute);

            if (isAttributeModifierEquals(thisModifiers, thatModifiers)) return true;
        }
        return false;
    }

    private static boolean isAttributeModifierEquals(List<? extends AttributeModifier> thisModifiers, List<? extends AttributeModifier> thatModifiers)
    {
        for (AttributeModifier thisModifier : thisModifiers)
        {
            for (AttributeModifier thatModifier : thatModifiers)
            {
                // Check except UUID
                if (thisModifier.getAmount() != thatModifier.getAmount() ||
                        thisModifier.getOperation() != thatModifier.getOperation() ||
                        thisModifier.getSlot() != thatModifier.getSlot())
                    return true;
                if (!Objects.equals(thisModifier.getName(), thatModifier.getName()))
                    return true;
            }
        }
        return false;
    }

    /**
     * {@link ItemStack} を 変換します。
     *
     * @param stack 変換する {@link ItemStack}
     * @return 変換されたもの
     */
    @SuppressWarnings("deprecation")
    public static ItemStackBean fromItemStack(@NotNull ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();

        Integer damage = null;
        if (meta instanceof Damageable)
            damage = ((Damageable) stack).getDamage();

        //noinspection DataFlowIssue
        return new ItemStackBean(
                stack.getType(),
                stack.getAmount(),
                meta.hasDisplayName() ? meta.getDisplayName(): null,
                meta.hasLocalizedName() ? meta.getLocalizedName(): null,
                meta.hasLore() ? meta.getLore(): null,
                meta.hasCustomModelData() ? meta.getCustomModelData(): null,
                stack.getEnchantments(),
                new ArrayList<>(meta.getItemFlags()),
                meta.isUnbreakable(),
                meta.getAttributeModifiers().asMap().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue())
                )),
                new ArrayList<>(meta.getPlaceableKeys()),
                new ArrayList<>(meta.getDestroyableKeys()),
                damage
        );
    }

    /**
     * {@link ItemStack} に変換します。
     *
     * @return 変換されたもの
     */
    @SuppressWarnings("deprecation")
    public ItemStack toItemStack()
    {
        ItemStack stack = new ItemStack(this.type, this.amount);
        ItemMeta meta = stack.getItemMeta();

        if (this.displayName != null)
            meta.setDisplayName(this.displayName);
        if (this.localizedName != null)
            meta.setLocalizedName(this.localizedName);
        if (!this.lore.isEmpty())
            meta.setLore(this.lore);
        if (this.customModelData != null)
            meta.setCustomModelData(this.customModelData);
        if (!this.enchantments.isEmpty())
            stack.addUnsafeEnchantments(this.enchantments);
        if (!this.itemFlags.isEmpty())
            meta.addItemFlags(this.itemFlags.toArray(new ItemFlag[0]));
        meta.setUnbreakable(this.unbreakable);
        if (!this.attributeModifiers.isEmpty())
        {
            for (Attribute attribute : this.attributeModifiers.keySet())
            {
                for (AttributeModifier modifier : this.attributeModifiers.get(attribute))
                {
                    meta.addAttributeModifier(attribute, modifier);
                }
            }
        }
        if (!this.placeableKeys.isEmpty())
            meta.setPlaceableKeys(this.placeableKeys);
        if (!this.destroyableKeys.isEmpty())
            meta.setDestroyableKeys(this.destroyableKeys);
        if (this.damage != null && meta instanceof Damageable)
            ((Damageable) meta).setDamage(this.damage);

        stack.setItemMeta(meta);

        return stack;
    }

    @Override
    public int hashCode()
    {
        int result = this.type.hashCode();
        result = 31 * result + this.amount;
        result = 31 * result + (this.displayName != null ? this.displayName.hashCode(): 0);
        result = 31 * result + (this.localizedName != null ? this.localizedName.hashCode(): 0);
        result = 31 * result + this.lore.hashCode();
        result = 31 * result + (this.customModelData != null ? this.customModelData.hashCode(): 0);
        result = 31 * result + this.enchantments.hashCode();
        result = 31 * result + this.itemFlags.hashCode();
        result = 31 * result + (this.unbreakable ? 1: 0);
        result = 31 * result + this.attributeModifiers.hashCode();
        result = 31 * result + this.placeableKeys.hashCode();
        result = 31 * result + this.destroyableKeys.hashCode();
        result = 31 * result + (this.damage != null ? this.damage.hashCode(): 0);
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ItemStackBean)) return false;

        ItemStackBean that = (ItemStackBean) o;

        if (this.amount != that.amount) return false;
        if (this.unbreakable != that.unbreakable) return false;
        if (this.type != that.type) return false;
        if (!Objects.equals(this.displayName, that.displayName)) return false;
        if (!Objects.equals(this.localizedName, that.localizedName)) return false;
        if (!Objects.equals(this.lore, that.lore)) return false;
        if (!Objects.equals(this.customModelData, that.customModelData))
            return false;
        if (!this.enchantments.equals(that.enchantments)) return false;
        if (!this.itemFlags.equals(that.itemFlags)) return false;
        if (!this.placeableKeys.equals(that.placeableKeys)) return false;
        if (!this.destroyableKeys.equals(that.destroyableKeys)) return false;

        if (isAttributeModifiersEquals(this.attributeModifiers, that.attributeModifiers)) return false;

        return Objects.equals(this.damage, that.damage);
    }
}
