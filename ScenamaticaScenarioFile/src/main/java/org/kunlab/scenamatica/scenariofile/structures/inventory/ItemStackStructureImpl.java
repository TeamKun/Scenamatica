package org.kunlab.scenamatica.scenariofile.structures.inventory;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.kyori.adventure.text.Component;
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
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class ItemStackStructureImpl implements ItemStackStructure
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

    public ItemStackStructureImpl(Material material, int amount)
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
            Map<String, Object> enchtansMap = MapUtils.checkAndCastMap(map.get(KEY_ENCHANTMENTS));
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

    private static Map<String, Object> serializeEnchantments(ItemStackStructure structure)
    {
        Map<String, Object> result = new HashMap<>();
        if (!structure.getEnchantments().isEmpty())
            for (Map.Entry<Enchantment, Integer> entry : structure.getEnchantments().entrySet())
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

    private static Map<String, Object> serializeAttributeModifiers(ItemStackStructure structure)
    {
        Map<String, Object> result = new HashMap<>();
        if (!structure.getAttributeModifiers().isEmpty())
        {
            for (Map.Entry<Attribute, List<AttributeModifier>> entry : structure.getAttributeModifiers().entrySet())
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
                Map<String, Object> valuesMap = MapUtils.checkAndCastMap(obj);

                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_AMOUNT, Number.class);   // double
                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_OPERATION, String.class);
                MapUtils.checkTypeIfContains(valuesMap, KEY_ATTRIBUTE_MODIFIER_SLOT, String.class);

                getAttributeFromString(name);
            }
        }

    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull ItemStackStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_TYPE, structure.getType());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_UNBREAKABLE, structure.getUnbreakable());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_AMOUNT, structure.getAmount());

        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DISPLAY_NAME, structure.getDisplayName());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_LOCALIZED_NAME, structure.getLocalizedName());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_CUSTOM_MODEL_DATA, structure.getCustomModelData());
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DAMAGE, structure.getDamage());

        MapUtils.putListIfNotEmpty(map, KEY_LORE, structure.getLore());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_PLACEABLES, structure.getPlaceableKeys());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_DESTROYABLES, structure.getDestroyableKeys());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_ITEM_FLAGS, structure.getItemFlags());

        MapUtils.putMapIfNotEmpty(map, KEY_ENCHANTMENTS, serializeEnchantments(structure));
        MapUtils.putMapIfNotEmpty(map, KEY_ATTRIBUTE_MODIFIERS, serializeAttributeModifiers(structure));


        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkMaterialNameIfContains(map, KEY_TYPE);
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
    public static ItemStackStructure deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        Material type = Utils.searchMaterial(MapUtils.getOrNull(map, KEY_TYPE));
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

        return new ItemStackStructureImpl(
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
                if (thisModifier.getAmount() != thatModifier.getAmount()
                        || thisModifier.getOperation() != thatModifier.getOperation()
                        || thisModifier.getSlot() != thatModifier.getSlot())
                    return true;
                if (!Objects.equals(thisModifier.getName(), thatModifier.getName()))
                    return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static ItemStackStructure of(@NotNull ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();

        Integer damage = null;
        if (meta instanceof Damageable)
            damage = ((Damageable) stack).getDamage();

        String displayName = null;
        String localizedName = null;
        List<String> lore = Collections.emptyList();
        Integer customModelData = null;
        List<ItemFlag> flags = Collections.emptyList();
        Boolean unbreakable = null;
        Map<Enchantment, Integer> enchantments = Collections.emptyMap();
        Map<Attribute, List<AttributeModifier>> attributeModifiers = Collections.emptyMap();
        List<Namespaced> placeableKeys = Collections.emptyList();
        List<Namespaced> destroyableKeys = Collections.emptyList();
        if (meta != null)
        {
            displayName = meta.hasDisplayName() ? meta.getDisplayName(): null;
            localizedName = meta.hasLocalizedName() ? meta.getLocalizedName(): null;
            if (meta.hasLore())
            {
                lore = meta.getLore();
                assert lore != null;
            }
            customModelData = meta.hasCustomModelData() ? meta.getCustomModelData(): null;
            flags = new ArrayList<>(meta.getItemFlags());
            unbreakable = meta.isUnbreakable();
            enchantments = new HashMap<>(stack.getEnchantments());
            if (meta.hasAttributeModifiers())
                attributeModifiers = new HashMap<>(Objects.requireNonNull(meta.getAttributeModifiers()).asMap()
                        .entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new ArrayList<>(e.getValue())
                        )));
            if (meta.hasPlaceableKeys())
                placeableKeys = new ArrayList<>(meta.getPlaceableKeys());
            if (meta.hasDestroyableKeys())
                destroyableKeys = new ArrayList<>(meta.getDestroyableKeys());
        }

        return new ItemStackStructureImpl(
                stack.getType(),
                stack.getAmount(),
                displayName,
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

    public static boolean isApplicable(Object o)
    {
        return o instanceof ItemStack;
    }

    @Override
    public String toString()
    {
        return "ItemStackStructure{" +
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
        if (!(o instanceof ItemStackStructure)) return false;

        ItemStackStructure that = (ItemStackStructure) o;

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

    @NotNull
    public ItemStack create()
    {
        if (this.type == null)
            throw new IllegalStateException("Unable to create ItemStack from ItemStackStructure: type is null");

        int amount = this.amount != null ? this.amount: 1;

        ItemStack stack = new ItemStack(this.type, amount);
        this.applyTo(stack);
        return stack;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void applyTo(ItemStack stack)
    {
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
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof ItemStack;
    }

    @Override
    public boolean isAdequate(ItemStack stack, boolean strict)
    {
        if (this.type != null)
            if (stack.getType() != this.type)
                return false;

        if (this.amount != null)
            if (stack.getAmount() != this.amount)
                return false;

        ItemMeta meta = stack.getItemMeta();

        if (this.displayName != null)
            if (meta == null || !TextUtils.isSameContent(meta.displayName(), this.displayName))
                return false;

        if (this.localizedName != null)
            if (meta == null || !this.localizedName.equals(meta.getLocalizedName()))
                return false;

        if (!this.lore.isEmpty())
        {
            List<String> expected = this.lore;
            List<Component> actual = meta == null ? null: meta.lore();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            // Lore を文字列に変換して比較する
            if (expected.stream().anyMatch(s -> actual.stream().noneMatch(c -> TextUtils.isSameContent(c, s))))
                return false;
        }

        if (this.customModelData != null)
            if (meta == null || !this.customModelData.equals(meta.getCustomModelData()))
                return false;

        if (!this.enchantments.isEmpty())
        {
            Map<Enchantment, Integer> expected = this.enchantments;
            Map<Enchantment, Integer> actual = stack.getEnchantments();

            if (strict && actual.size() != expected.size())
                return false;

            for (Map.Entry<Enchantment, Integer> entry : expected.entrySet())
                if (!actual.containsKey(entry.getKey()) || !actual.get(entry.getKey()).equals(entry.getValue()))
                    return false;
        }

        if (!this.itemFlags.isEmpty())
        {
            List<ItemFlag> expected = this.itemFlags;
            Set<ItemFlag> actual = meta == null ? null: meta.getItemFlags();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(f -> actual.stream().noneMatch(f::equals)))
                return false;
        }

        if (Boolean.TRUE.equals(this.unbreakable))
            if (meta == null || !meta.isUnbreakable())
                return false;

        if (!this.attributeModifiers.isEmpty())
        {
            Map<Attribute, List<AttributeModifier>> expected = this.attributeModifiers;
            Multimap<Attribute, AttributeModifier> actual = meta == null ? null: meta.getAttributeModifiers();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            for (Map.Entry<Attribute, List<AttributeModifier>> entry : expected.entrySet())
            {
                if (!actual.containsKey(entry.getKey()) || actual.get(entry.getKey()).size() != entry.getValue().size())
                    return false;

                for (AttributeModifier modifier : entry.getValue())
                    if (actual.get(entry.getKey()).stream().noneMatch(m -> m.equals(modifier)))
                        return false;
            }
        }

        if (!this.placeableKeys.isEmpty())
        {
            List<Namespaced> expected = this.placeableKeys;
            Set<Namespaced> actual = meta == null ? null: meta.getPlaceableKeys();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(k -> actual.stream().noneMatch(k::equals)))
                return false;
        }

        if (!this.destroyableKeys.isEmpty())
        {
            List<Namespaced> expected = this.destroyableKeys;
            Set<Namespaced> actual = meta == null ? null: meta.getDestroyableKeys();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(k -> actual.stream().noneMatch(k::equals)))
                return false;
        }

        if (this.damage != null)
            // noinspection deprecation
            return stack.getDurability() == this.damage;

        return true;
    }
}
