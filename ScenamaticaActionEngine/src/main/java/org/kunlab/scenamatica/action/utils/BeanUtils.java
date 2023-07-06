package org.kunlab.scenamatica.action.utils;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;

import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class BeanUtils
{
    public static boolean isSame(ItemStackBean bean, ItemStack stack, boolean strict)
    {
        if (bean == null || stack == null)
            return bean == null && stack == null;

        if (bean.getType() != null)
            if (stack.getType() != bean.getType())
                return false;

        if (bean.getAmount() != null)
            if (stack.getAmount() != bean.getAmount())
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

        if (Boolean.TRUE.equals(bean.getUnbreakable()))
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

    public static boolean isSame(BlockBean blockBean, Block block, World world)
    {
        if (blockBean == null || block == null)
            return blockBean == null && block == null;

        if (blockBean.getType() != null && blockBean.getType() != block.getType())
            return false;

        Location expectedLoc = blockBean.getLocation().clone();
        Location actualLoc = block.getLocation();
        if (expectedLoc.getWorld() == null)
            expectedLoc.setWorld(world);

        if (!expectedLoc.equals(actualLoc))
            return false;

        if (blockBean.getBiome() != null && blockBean.getBiome() != block.getBiome())
            return false;

        if (!blockBean.getMetadata().isEmpty())
        {
            Map<String, Object> expected = blockBean.getMetadata();
            for (Map.Entry<String, Object> entry : expected.entrySet())
            {
                List<MetadataValue> actual = block.getMetadata(entry.getKey());
                if (actual.isEmpty()
                        || actual.stream().noneMatch(v -> v.asString().equals(entry.getValue().toString())))
                    return false;
            }
        }

        return true;
    }

    public static boolean isSame(@NotNull BlockBean blockBean, @NotNull Block block, @NotNull ScenarioEngine engine)
    {
        return isSame(blockBean, block, engine.getContext().getStage());
    }

    public static boolean isSame(@NotNull InventoryBean inventoryBean, @NotNull Inventory inventory, boolean strict)
    {
        if (strict && !(inventoryBean.getSize() == null || inventoryBean.getSize() == inventory.getSize()))
            return false;


        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStackBean expected = inventoryBean.getMainContents().get(i);
            ItemStack actual = inventory.getItem(i);

            if (!isSame(expected, actual, strict))
                return false;
        }

        if (inventory instanceof PlayerInventory && inventoryBean instanceof PlayerInventoryBean)
        {
            PlayerInventoryBean playerInventoryBean = (PlayerInventoryBean) inventoryBean;
            PlayerInventory playerInventory = (PlayerInventory) inventory;
            return isSame(playerInventoryBean, playerInventory, strict);
        }

        return true;
    }

    public static boolean isSame(@NotNull PlayerInventoryBean playerInventoryBean, @NotNull PlayerInventory playerInventory)
    {
        return isSame(playerInventoryBean, playerInventory, true);
    }

    private static boolean isSame(@NotNull PlayerInventoryBean playerInventoryBean,
                                  @NotNull PlayerInventory playerInventory, boolean checkInventory)
    {
        ItemStackBean[] expectedArmors = playerInventoryBean.getArmorContents();
        ItemStack[] actualArmors = playerInventory.getArmorContents();

        if (expectedArmors != null)
            for (int i = 0; i < expectedArmors.length; i++)
                if (!isSame(expectedArmors[i], actualArmors[i], true))
                    return false;

        ItemStackBean expectedMainHand = playerInventoryBean.getMainHand();
        ItemStack actualMainHand = playerInventory.getItemInMainHand();
        if (expectedMainHand != null && !isSame(expectedMainHand, actualMainHand, true))
            return false;

        ItemStackBean expectedOffHand = playerInventoryBean.getOffHand();
        ItemStack actualOffHand = playerInventory.getItemInOffHand();
        if (expectedOffHand != null && !isSame(expectedOffHand, actualOffHand, true))
            return false;

        return !checkInventory || isSame(playerInventoryBean, playerInventory, true);
    }
}
