package net.kunmc.lab.scenamatica.action.utils;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class BeanUtils
{
    public static boolean isSame(ItemStackBean bean, ItemStack stack, boolean strict)
    {
        if (bean == null)
            return stack == null;
        else if (stack == null)
            return false;

        if (!(bean.getType() == stack.getType() && bean.getAmount() == stack.getAmount()))
            return false;

        ItemMeta meta = stack.getItemMeta();

        if (bean.getDisplayName() != null)
            if (meta == null || !TextUtils.isSameContent(meta.displayName(), bean.getDisplayName()))
                return false;

        if (bean.getLocalizedName() != null)
            if (meta == null || !bean.getLocalizedName().equals(meta.getLocalizedName()))
                return false;

        if (!bean.getLore().isEmpty())
        {
            List<String> expected = bean.getLore();
            List<Component> actual = meta == null ? null: meta.lore();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            // Lore を文字列に変換して比較する
            if (expected.stream().anyMatch(s -> actual.stream().noneMatch(c -> TextUtils.isSameContent(c, s))))
                return false;
        }

        if (bean.getCustomModelData() != null)
            if (meta == null || !bean.getCustomModelData().equals(meta.getCustomModelData()))
                return false;

        if (!bean.getEnchantments().isEmpty())
        {
            Map<Enchantment, Integer> expected = bean.getEnchantments();
            Map<Enchantment, Integer> actual = stack.getEnchantments();

            if (strict && actual.size() != expected.size())
                return false;

            for (Map.Entry<Enchantment, Integer> entry : expected.entrySet())
                if (!actual.containsKey(entry.getKey()) || !actual.get(entry.getKey()).equals(entry.getValue()))
                    return false;
        }

        if (!bean.getItemFlags().isEmpty())
        {
            List<ItemFlag> expected = bean.getItemFlags();
            Set<ItemFlag> actual = meta == null ? null: meta.getItemFlags();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(f -> actual.stream().noneMatch(f::equals)))
                return false;
        }

        if (bean.isUnbreakable())
            if (meta == null || !meta.isUnbreakable())
                return false;

        if (!bean.getAttributeModifiers().isEmpty())
        {
            Map<Attribute, List<AttributeModifier>> expected = bean.getAttributeModifiers();
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

        if (!bean.getPlaceableKeys().isEmpty())
        {
            List<Namespaced> expected = bean.getPlaceableKeys();
            Set<Namespaced> actual = meta == null ? null: meta.getPlaceableKeys();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(k -> actual.stream().noneMatch(k::equals)))
                return false;
        }

        if (!bean.getDestroyableKeys().isEmpty())
        {
            List<Namespaced> expected = bean.getDestroyableKeys();
            Set<Namespaced> actual = meta == null ? null: meta.getDestroyableKeys();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(k -> actual.stream().noneMatch(k::equals)))
                return false;
        }

        if (bean.getDamage() != null)
            // noinspection deprecation
            return stack.getDurability() == bean.getDamage();

        return true;
    }
}
