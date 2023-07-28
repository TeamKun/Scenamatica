package org.kunlab.scenamatica.scenariofile.beans.inventory;

import com.destroystokyo.paper.Namespaced;
import lombok.AllArgsConstructor;
import lombok.Value;
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
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class ItemStackBeanImpl implements ItemStackBean
{
    Material type;
    Integer amount;
    String displayName;
    String localizedName;
    @NotNull
    List<String> lore;
    Integer customModelData;
    @NotNull
    Map<Enchantment, Integer> enchantments;
    @NotNull
    List<ItemFlag> itemFlags;
    Boolean unbreakable;
    @NotNull
    Map<Attribute, List<AttributeModifier>> attributeModifiers;
    @NotNull
    List<Namespaced> placeableKeys;
    @NotNull
    List<Namespaced> destroyableKeys;
    Integer damage;

    public ItemStackBeanImpl(Material material, int amount)
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
                null,
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
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
                Collections.<String, Object>emptyMap()
        ).entrySet())
        {
            Enchantment enchantment = Enchantment.getByKey(NamespaceUtils.fromString(entry.getKey()));
            if (enchantment == null)
                enchantment = Enchantment.getByName(entry.getKey());

            int level = (int) entry.getValue();
            result.put(enchantment, level);
        }

        return result;
    }

    private static Map<String, Object> serializeEnchantments(ItemStackBean bean)
    {
        Map<String, Object> result = new HashMap<>();
        if (!bean.getEnchantments().isEmpty())
            for (Map.Entry<Enchantment, Integer> entry : bean.getEnchantments().entrySet())
            {
                String key = NamespaceUtils.toString(entry.getKey().getKey());
                Integer value = entry.getValue();
                result.put(key, value);
            }

        return result;
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
                UUID uuid;
                if (attrs.containsKey(KEY_ATTRIBUTE_MODIFIER_UUID))
                    uuid = UUID.fromString(attrs.get(KEY_ATTRIBUTE_MODIFIER_UUID).toString());
                else
                    uuid = UUID.randomUUID();
                double attrAmount = MapUtils.getOrDefault(attrs, KEY_ATTRIBUTE_MODIFIER_AMOUNT, 0.0);
                AttributeModifier.Operation attrOperation =
                        MapUtils.getAsEnum(attrs, KEY_ATTRIBUTE_MODIFIER_OPERATION, AttributeModifier.Operation.class);
                EquipmentSlot slot = MapUtils.getAsEnumOrNull(attrs, KEY_ATTRIBUTE_MODIFIER_SLOT, EquipmentSlot.class);

                modifiers.add(new AttributeModifier(
                        uuid,
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

    private static Map<String, Object> serializeAttributeModifiers(ItemStackBean bean)
    {
        Map<String, Object> result = new HashMap<>();
        if (!bean.getAttributeModifiers().isEmpty())
        {
            for (Map.Entry<Attribute, List<AttributeModifier>> entry : bean.getAttributeModifiers().entrySet())
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

                getAttributeFromString(name);
            }
        }

    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull ItemStackBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_TYPE, bean.getType());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_UNBREAKABLE, bean.getUnbreakable());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_AMOUNT, bean.getAmount());

        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DISPLAY_NAME, bean.getDisplayName());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_LOCALIZED_NAME, bean.getLocalizedName());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_CUSTOM_MODEL_DATA, bean.getCustomModelData());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DAMAGE, bean.getDamage());

        MapUtils.putListIfNotEmpty(map, KEY_LORE, bean.getLore());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_PLACEABLES, bean.getPlaceableKeys());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_DESTROYABLES, bean.getDestroyableKeys());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_ITEM_FLAGS, bean.getItemFlags());

        MapUtils.putMapIfNotEmpty(map, KEY_ENCHANTMENTS, serializeEnchantments(bean));
        MapUtils.putMapIfNotEmpty(map, KEY_ATTRIBUTE_MODIFIERS, serializeAttributeModifiers(bean));


        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkEnumNameIfContains(map, KEY_TYPE, Material.class);
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

    @NotNull
    public static ItemStackBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        Material type = MapUtils.getAsEnumOrNull(map, KEY_TYPE, Material.class);
        Integer amount = MapUtils.getOrNull(map, KEY_AMOUNT);
        String name = MapUtils.getOrNull(map, KEY_DISPLAY_NAME);
        String localizedName = MapUtils.getOrNull(map, KEY_LOCALIZED_NAME);
        List<String> lore = MapUtils.getOrDefault(map, KEY_LORE, Collections.emptyList());
        Integer customModelData = MapUtils.getOrNull(map, KEY_CUSTOM_MODEL_DATA);
        List<ItemFlag> flags = MapUtils.getAsEnumOrEmptyList(map, KEY_ITEM_FLAGS, ItemFlag.class);
        Boolean unbreakable = MapUtils.getOrNull(map, KEY_UNBREAKABLE);
        Integer damage = MapUtils.getOrNull(map, KEY_DAMAGE);

        List<Namespaced> placeableKeys = new ArrayList<>();
        for (String key : MapUtils.getOrDefault(map, KEY_PLACEABLES, Collections.<String>emptyList()))
            placeableKeys.add(NamespaceUtils.fromString(key));

        List<Namespaced> destroyableKeys = new ArrayList<>();
        for (String key : MapUtils.getOrDefault(map, KEY_DESTROYABLES, Collections.<String>emptyList()))
            destroyableKeys.add(NamespaceUtils.fromString(key));

        Map<Enchantment, Integer> enchantments = deserializeEnchantments(map);
        Map<Attribute, List<AttributeModifier>> attributeModifiers = deserializeAttributeModifiers(map);

        return new ItemStackBeanImpl(
                type,
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

    @SuppressWarnings("deprecation")
    public static ItemStackBean fromItemStack(@NotNull ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();

        Integer damage = null;
        if (meta instanceof Damageable)
            damage = ((Damageable) stack).getDamage();

        //noinspection DataFlowIssue
        return new ItemStackBeanImpl(
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

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public ItemStack toItemStack()
    {
        if (this.type == null)
            throw new IllegalStateException("Unable to create ItemStack from ItemStackBean: type is null");

        int amount = this.amount != null ? this.amount: 1;

        ItemStack stack = new ItemStack(this.type, amount);
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
        if (this.unbreakable != null)
            meta.setUnbreakable(this.unbreakable);
        if (!this.attributeModifiers.isEmpty())
            for (Attribute attribute : this.attributeModifiers.keySet())
                for (AttributeModifier modifier : this.attributeModifiers.get(attribute))
                    meta.addAttributeModifier(attribute, modifier);

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
        result = 31 * result + (this.amount != null ? this.amount.hashCode(): 0);
        result = 31 * result + (this.displayName != null ? this.displayName.hashCode(): 0);
        result = 31 * result + (this.localizedName != null ? this.localizedName.hashCode(): 0);
        result = 31 * result + this.lore.hashCode();
        result = 31 * result + (this.customModelData != null ? this.customModelData.hashCode(): 0);
        result = 31 * result + this.enchantments.hashCode();
        result = 31 * result + this.itemFlags.hashCode();
        result = 31 * result + (this.unbreakable != null ? this.unbreakable.hashCode(): 0);
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

        if (!Objects.equals(this.amount, that.getAmount())) return false;
        if (this.unbreakable != that.getUnbreakable()) return false;
        if (this.type != that.getType()) return false;
        if (!Objects.equals(this.displayName, that.getDisplayName())) return false;
        if (!Objects.equals(this.localizedName, that.getLocalizedName())) return false;
        if (!Objects.equals(this.lore, that.getLore())) return false;
        if (!Objects.equals(this.customModelData, that.getCustomModelData()))
            return false;
        if (!this.enchantments.equals(that.getEnchantments())) return false;
        if (!this.itemFlags.equals(that.getItemFlags())) return false;
        if (!this.placeableKeys.equals(that.getPlaceableKeys())) return false;
        if (!this.destroyableKeys.equals(that.getDestroyableKeys())) return false;

        if (isAttributeModifiersEquals(this.attributeModifiers, that.getAttributeModifiers())) return false;

        return Objects.equals(this.damage, that.getDamage());
    }


}
