package org.kunlab.scenamatica.structures.minecraft.inventory;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
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
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.structures.StructureMappers;
import org.kunlab.scenamatica.structures.StructureValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
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
    private static void validateEnchantments(StructuredYamlNode node) throws YamlParsingException
    {
        if (!node.containsKey(KEY_ENCHANTMENTS))
            return;

        Map<StructuredYamlNode, StructuredYamlNode> enchtansMap = node.get(KEY_ENCHANTMENTS).asNodeMap();
        for (Map.Entry<StructuredYamlNode, StructuredYamlNode> entry : enchtansMap.entrySet())
        {
            entry.getKey().validate(keyNode -> {
                if (Enchantment.getByKey(NamespaceUtils.fromString(keyNode.asString())) == null
                        && Enchantment.getByName(keyNode.asString()) == null)
                    throw new IllegalArgumentException();
                return null;
            }, "Invalid enchantment key: " + entry.getKey());
            entry.getValue().isType(YAMLNodeType.NUMBER);
        }
    }

    @SuppressWarnings("deprecation")
    private static Map<Enchantment, Integer> deserializeEnchantments(StructuredYamlNode node) throws YamlParsingException
    {
        Map<Enchantment, Integer> result = new HashMap<>();
        for (Map.Entry<StructuredYamlNode, StructuredYamlNode> entry : node.get(KEY_ENCHANTMENTS).asNodeMap().entrySet())
        {
            String key = entry.getKey().asString();
            Enchantment enchantment = Enchantment.getByKey(NamespaceUtils.fromString(key));
            if (enchantment == null)
                enchantment = Enchantment.getByName(key);

            int level = entry.getValue().asInteger();
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

    private static Map<Attribute, List<AttributeModifier>> deserializeAttributeModifiers(StructuredYamlNode node) throws YamlParsingException
    {
        Map<Attribute, List<AttributeModifier>> result = new HashMap<>();

        for (Map.Entry<StructuredYamlNode, StructuredYamlNode> entry : node.get(KEY_ATTRIBUTE_MODIFIERS).asNodeMap().entrySet())
        {
            List<AttributeModifier> modifiers = new ArrayList<>();
            for (StructuredYamlNode attrs : entry.getValue().asList())
            {
                UUID uuid;
                if (attrs.containsKey(KEY_ATTRIBUTE_MODIFIER_UUID))
                    uuid = attrs.get(KEY_ATTRIBUTE_MODIFIER_UUID).getAs(StructureMappers.UUID);
                else
                    uuid = UUID.randomUUID();
                double attrAmount = attrs.get(KEY_ATTRIBUTE_MODIFIER_AMOUNT).asDouble();
                AttributeModifier.Operation attrOperation =
                        attrs.get(KEY_ATTRIBUTE_MODIFIER_OPERATION).getAs(StructureMappers.enumName(AttributeModifier.Operation.class));
                EquipmentSlot slot = attrs.get(KEY_ATTRIBUTE_MODIFIER_SLOT).getAs(StructureMappers.enumName(EquipmentSlot.class), null);

                modifiers.add(new AttributeModifier(
                        uuid,
                        attrs.get(KEY_ATTRIBUTE_MODIFIER_NAME).asString(),
                        attrAmount,
                        attrOperation,
                        slot
                ));
            }

            result.put(getAttributeFromString(entry.getKey().asString()), modifiers);
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

                result.put(getAttributeString(entry.getKey()), list);
            }
        }

        return result;
    }

    private static String getAttributeString(Attribute attribute)
    {
        String attributeName;
        switch (attribute)
        {
            case GENERIC_MAX_HEALTH:
                attributeName = "generic.max_health";
                break;
            case GENERIC_FOLLOW_RANGE:
                attributeName = "generic.follow_range";
                break;
            case GENERIC_KNOCKBACK_RESISTANCE:
                attributeName = "generic.knockback_resistance";
                break;
            case GENERIC_MOVEMENT_SPEED:
                attributeName = "generic.movement_speed";
                break;
            case GENERIC_FLYING_SPEED:
                attributeName = "generic.flying_speed";
                break;
            case GENERIC_ATTACK_DAMAGE:
                attributeName = "generic.attack_damage";
                break;
            case GENERIC_ATTACK_SPEED:
                attributeName = "generic.attack_speed";
                break;
            case GENERIC_ARMOR:
                attributeName = "generic.armor";
                break;
            case GENERIC_ARMOR_TOUGHNESS:
                attributeName = "generic.armor_toughness";
                break;
            case GENERIC_LUCK:
                attributeName = "generic.luck";
                break;
            case HORSE_JUMP_STRENGTH:
                attributeName = "horse.jump_strength";
                break;
            case ZOMBIE_SPAWN_REINFORCEMENTS:
                attributeName = "zombie.spawn_reinforcements";
                break;
            default:
                return null;
        }
        return attributeName.startsWith("generic.") ? attributeName.substring("generic.".length()): attributeName;
    }

    private static Attribute getAttributeFromString(String name)
    {
        if (name.startsWith("generic."))
            name = name.substring("generic.".length());

        switch (name)
        {
            case "max_health":
                return Attribute.GENERIC_MAX_HEALTH;
            case "follow_range":
                return Attribute.GENERIC_FOLLOW_RANGE;
            case "knockback_resistance":
                return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case "movement_speed":
                return Attribute.GENERIC_MOVEMENT_SPEED;
            case "flying_speed":
                return Attribute.GENERIC_FLYING_SPEED;
            case "attack_damage":
                return Attribute.GENERIC_ATTACK_DAMAGE;
            case "attack_speed":
                return Attribute.GENERIC_ATTACK_SPEED;
            case "armor":
                return Attribute.GENERIC_ARMOR;
            case "armor_toughness":
                return Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "luck":
                return Attribute.GENERIC_LUCK;
            case "horse.jump_strength":
                return Attribute.HORSE_JUMP_STRENGTH;
            case "zombie.spawn_reinforcements":
                return Attribute.ZOMBIE_SPAWN_REINFORCEMENTS;
            default:
                return null;
        }
    }

    private static void validateAttributeModifiersNode(StructuredYamlNode node) throws YamlParsingException
    {
        if (!node.containsKey(KEY_ATTRIBUTE_MODIFIERS))
            return;

        Map<StructuredYamlNode, StructuredYamlNode> attributesMap = node.get(KEY_ATTRIBUTE_MODIFIERS).asNodeMap();

        for (Map.Entry<StructuredYamlNode, StructuredYamlNode> entry : attributesMap.entrySet())
        {
            StructuredYamlNode name = entry.getKey();
            for (StructuredYamlNode attributeModifier : entry.getValue().asList())
            {
                attributeModifier.get(KEY_ATTRIBUTE_MODIFIER_AMOUNT).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
                attributeModifier.get(KEY_ATTRIBUTE_MODIFIER_OPERATION).ensureTypeOfIfExists(YAMLNodeType.STRING);
                attributeModifier.get(KEY_ATTRIBUTE_MODIFIER_SLOT).ensureTypeOfIfExists(YAMLNodeType.STRING);
            }

            name.validate((nameNode) -> {
                if (getAttributeFromString(nameNode.asString()) == null)
                    throw new IllegalArgumentException();
                return null;
            }, "Not a valid attribute name: " + name.asString());
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
        MapUtils.putPrimitiveOrStrIfNotNull(map, KEY_DAMAGE, structure.getDamage());

        MapUtils.putListIfNotEmpty(map, KEY_LORE, structure.getLore());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_PLACEABLES, structure.getPlaceableKeys());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_DESTROYABLES, structure.getDestroyableKeys());
        MapUtils.putPrimitiveOrStrListIfNotEmpty(map, KEY_ITEM_FLAGS, structure.getItemFlags());

        MapUtils.putMapIfNotEmpty(map, KEY_ENCHANTMENTS, serializeEnchantments(structure));
        MapUtils.putMapIfNotEmpty(map, KEY_ATTRIBUTE_MODIFIERS, serializeAttributeModifiers(structure));


        return map;
    }

    public static void validate(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        node.get(KEY_TYPE).validateIfExists(StructureValidators.MATERIAL_NAME);
        node.get(KEY_AMOUNT).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_DISPLAY_NAME).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_LOCALIZED_NAME).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_LORE).ensureTypeOfIfExists(YAMLNodeType.LIST);
        node.get(KEY_CUSTOM_MODEL_DATA).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_ITEM_FLAGS).ensureTypeOfIfExists(YAMLNodeType.LIST);
        node.get(KEY_UNBREAKABLE).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
        node.get(KEY_DAMAGE).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_PLACEABLES).ensureTypeOfIfExists(YAMLNodeType.LIST);
        node.get(KEY_DESTROYABLES).ensureTypeOfIfExists(YAMLNodeType.LIST);

        validateEnchantments(node);
        validateAttributeModifiersNode(node);
    }

    @NotNull
    public static ItemStackStructure deserialize(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        validate(node);

        Material type = node.get(KEY_TYPE).getAs(StructureMappers.MATERIAL_NAME);
        Integer amount = node.get(KEY_AMOUNT).asInteger(null);
        String name = node.get(KEY_DISPLAY_NAME).asString(null);
        String localizedName = node.get(KEY_LOCALIZED_NAME).asString(null);
        List<String> lore = node.get(KEY_LORE).asList(StructuredYamlNode::asString);
        List<ItemFlag> flags = node.get(KEY_ITEM_FLAGS).asList(StructureMappers.enumName(ItemFlag.class));
        Boolean unbreakable = node.get(KEY_UNBREAKABLE).asBoolean(null);
        Integer damage = node.get(KEY_DAMAGE).asInteger(null);

        List<Namespaced> placeableKeys = node.get(KEY_PLACEABLES).asList(StructureMappers.NAMESPACED);
        List<Namespaced> destroyableKeys = node.get(KEY_DESTROYABLES).asList(StructureMappers.NAMESPACED);

        Map<Enchantment, Integer> enchantments = deserializeEnchantments(node);
        Map<Attribute, List<AttributeModifier>> attributeModifiers = deserializeAttributeModifiers(node);

        return new ItemStackStructureImpl(
                type,
                amount,
                name,
                localizedName,
                lore,
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

    public static ItemStackStructure of(@NotNull ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();

        Integer damage = null;
        if (meta instanceof Damageable)
            damage = ((Damageable) meta).getDamage();

        String displayName = null;
        String localizedName = null;
        List<String> lore = Collections.emptyList();
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

    @Override
    public void applyTo(@NotNull ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();

        if (this.displayName != null)
            meta.setDisplayName(this.displayName);
        if (this.localizedName != null)
            meta.setLocalizedName(this.localizedName);
        if (!this.lore.isEmpty())
            meta.setLore(this.lore);
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
    public boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof ItemStack;
    }

    @Override
    public boolean isAdequate(@Nullable ItemStack stack, boolean strict)
    {
        if (stack == null)
            return false;

        if (this.type != null)
            if (stack.getType() != this.type)
                return false;

        if (this.amount != null)
            if (stack.getAmount() != this.amount)
                return false;

        ItemMeta meta = stack.getItemMeta();

        if (this.displayName != null)
            if (meta == null || !this.displayName.equalsIgnoreCase(meta.getDisplayName()))
                return false;

        if (this.localizedName != null)
            if (meta == null || !this.localizedName.equals(meta.getLocalizedName()))
                return false;

        if (!this.lore.isEmpty())
        {
            List<String> expected = this.lore;
            List<String> actual = meta == null ? null: meta.getLore();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            Predicate<String> predicate;
            if (strict)
                predicate = s -> actual.stream().anyMatch(s::equalsIgnoreCase);
            else
                predicate = s -> actual.stream().anyMatch(s::contains);

            if (expected.stream().anyMatch(s -> actual.stream().noneMatch(predicate)))
                return false;
        }

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
