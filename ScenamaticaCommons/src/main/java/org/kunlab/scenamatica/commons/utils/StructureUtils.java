package org.kunlab.scenamatica.commons.utils;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public class StructureUtils
{
    public static boolean isSame(ItemStackStructure structure, ItemStack stack, boolean strict)
    {
        if (structure == null || stack == null)
            return structure == null && stack == null;

        if (structure.getType() != null)
            if (stack.getType() != structure.getType())
                return false;

        if (structure.getAmount() != null)
            if (stack.getAmount() != structure.getAmount())
                return false;

        ItemMeta meta = stack.getItemMeta();

        if (structure.getDisplayName() != null)
            if (meta == null || !TextUtils.isSameContent(meta.displayName(), structure.getDisplayName()))
                return false;

        if (structure.getLocalizedName() != null)
            if (meta == null || !structure.getLocalizedName().equals(meta.getLocalizedName()))
                return false;

        if (!structure.getLore().isEmpty())
        {
            List<String> expected = structure.getLore();
            List<Component> actual = meta == null ? null: meta.lore();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            // Lore を文字列に変換して比較する
            if (expected.stream().anyMatch(s -> actual.stream().noneMatch(c -> TextUtils.isSameContent(c, s))))
                return false;
        }

        if (structure.getCustomModelData() != null)
            if (meta == null || !structure.getCustomModelData().equals(meta.getCustomModelData()))
                return false;

        if (!structure.getEnchantments().isEmpty())
        {
            Map<Enchantment, Integer> expected = structure.getEnchantments();
            Map<Enchantment, Integer> actual = stack.getEnchantments();

            if (strict && actual.size() != expected.size())
                return false;

            for (Map.Entry<Enchantment, Integer> entry : expected.entrySet())
                if (!actual.containsKey(entry.getKey()) || !actual.get(entry.getKey()).equals(entry.getValue()))
                    return false;
        }

        if (!structure.getItemFlags().isEmpty())
        {
            List<ItemFlag> expected = structure.getItemFlags();
            Set<ItemFlag> actual = meta == null ? null: meta.getItemFlags();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(f -> actual.stream().noneMatch(f::equals)))
                return false;
        }

        if (Boolean.TRUE.equals(structure.getUnbreakable()))
            if (meta == null || !meta.isUnbreakable())
                return false;

        if (!structure.getAttributeModifiers().isEmpty())
        {
            Map<Attribute, List<AttributeModifier>> expected = structure.getAttributeModifiers();
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

        if (!structure.getPlaceableKeys().isEmpty())
        {
            List<Namespaced> expected = structure.getPlaceableKeys();
            Set<Namespaced> actual = meta == null ? null: meta.getPlaceableKeys();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(k -> actual.stream().noneMatch(k::equals)))
                return false;
        }

        if (!structure.getDestroyableKeys().isEmpty())
        {
            List<Namespaced> expected = structure.getDestroyableKeys();
            Set<Namespaced> actual = meta == null ? null: meta.getDestroyableKeys();

            if (actual == null || (strict && actual.size() != expected.size()))
                return false;

            if (expected.stream().anyMatch(k -> actual.stream().noneMatch(k::equals)))
                return false;
        }

        if (structure.getDamage() != null)
            // noinspection deprecation
            return stack.getDurability() == structure.getDamage();

        return true;
    }

    public static boolean isSame(BlockStructure blockStructure, Block block)
    {
        if (blockStructure == null || block == null)
            return blockStructure == null && block == null;

        if (blockStructure.getType() != null && blockStructure.getType() != block.getType())
            return false;

        Location expectedLoc = blockStructure.getLocation();
        Location actualLoc = block.getLocation();
        if (expectedLoc != null)  // TODO: Refactor: to LocationStructure
        {
            if (Double.doubleToLongBits(expectedLoc.getX()) != Double.doubleToLongBits(actualLoc.getX()))
                return false;
            if (Double.doubleToLongBits(expectedLoc.getY()) != Double.doubleToLongBits(actualLoc.getY()))
                return false;
            if (Double.doubleToLongBits(expectedLoc.getZ()) != Double.doubleToLongBits(actualLoc.getZ()))
                return false;

            if (Float.floatToIntBits(expectedLoc.getYaw()) != Float.floatToIntBits(actualLoc.getYaw()))
                return false;
            if (Float.floatToIntBits(expectedLoc.getPitch()) != Float.floatToIntBits(actualLoc.getPitch()))
                return false;

            if (expectedLoc.getWorld() != null && !expectedLoc.getWorld().equals(actualLoc.getWorld()))
                return false;
        }

        if (blockStructure.getBiome() != null && blockStructure.getBiome() != block.getBiome())
            return false;

        if (!blockStructure.getMetadata().isEmpty())
        {
            Map<String, Object> expected = blockStructure.getMetadata();
            for (Map.Entry<String, Object> entry : expected.entrySet())
            {
                List<MetadataValue> actual = block.getMetadata(entry.getKey());
                if (actual.isEmpty()
                        || actual.stream().noneMatch(v -> v.asString().equals(entry.getValue().toString())))
                    return false;
            }
        }

        if (!blockStructure.getBlockData().isEmpty())
        {
            Map<String, Object> expected = blockStructure.getBlockData();
            Map<String, Object> actual = BlockDataParser.toMap(block.getBlockData());
            if (!MapUtils.isAdequate(expected, actual))
                return false;
        }

        if (blockStructure.getBlockState() != null)
        {
            byte expected = blockStructure.getBlockState();
            // noinspection deprecation
            byte actual = block.getState().getData().getData();

            return expected == actual;
        }

        return true;
    }

    public static boolean isSame(@NotNull InventoryStructure inventoryStructure, @NotNull Inventory inventory, boolean strict)
    {
        if (strict && !(inventoryStructure.getSize() == null || inventoryStructure.getSize() == inventory.getSize()))
            return false;


        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStackStructure expected = inventoryStructure.getMainContents().get(i);
            ItemStack actual = inventory.getItem(i);

            if (!isSame(expected, actual, strict))
                return false;
        }

        if (inventory instanceof PlayerInventory && inventoryStructure instanceof PlayerInventoryStructure)
        {
            PlayerInventoryStructure playerInventoryStructure = (PlayerInventoryStructure) inventoryStructure;
            PlayerInventory playerInventory = (PlayerInventory) inventory;
            return isSame(playerInventoryStructure, playerInventory, strict);
        }

        return true;
    }

    public static boolean isSame(@NotNull PlayerInventoryStructure playerInventoryStructure, @NotNull PlayerInventory playerInventory)
    {
        return isSame(playerInventoryStructure, playerInventory, true);
    }

    private static boolean isSame(@NotNull PlayerInventoryStructure playerInventoryStructure,
                                  @NotNull PlayerInventory playerInventory, boolean checkInventory)
    {
        ItemStackStructure[] expectedArmors = playerInventoryStructure.getArmorContents();
        ItemStack[] actualArmors = playerInventory.getArmorContents();

        if (expectedArmors != null)
            for (int i = 0; i < expectedArmors.length; i++)
                if (!isSame(expectedArmors[i], actualArmors[i], true))
                    return false;

        ItemStackStructure expectedMainHand = playerInventoryStructure.getMainHand();
        ItemStack actualMainHand = playerInventory.getItemInMainHand();
        if (expectedMainHand != null && !isSame(expectedMainHand, actualMainHand, true))
            return false;

        ItemStackStructure expectedOffHand = playerInventoryStructure.getOffHand();
        ItemStack actualOffHand = playerInventory.getItemInOffHand();
        if (expectedOffHand != null && !isSame(expectedOffHand, actualOffHand, true))
            return false;

        return !checkInventory || isSame(playerInventoryStructure, playerInventory, true);
    }

    public static void applyEntityStructureData(@NotNull EntityStructure structure, @NotNull Entity entity)
    {
        if (structure.getCustomName() != null)
            entity.setCustomName(structure.getCustomName());
        if (structure.getVelocity() != null)
            entity.setVelocity(structure.getVelocity());
        if (structure.getCustomNameVisible() != null)
            entity.setCustomNameVisible(structure.getCustomNameVisible());
        if (structure.getGlowing() != null)
            entity.setGlowing(structure.getGlowing());
        if (structure.getGravity() != null)
            entity.setGravity(structure.getGravity());
        if (structure.getSilent() != null)
            entity.setSilent(structure.getSilent());
        if (structure.getInvulnerable() != null)
            entity.setInvulnerable(structure.getInvulnerable());
        if (structure.getCustomNameVisible() != null)
            entity.setCustomNameVisible(structure.getCustomNameVisible());
        if (structure.getInvulnerable() != null)
            entity.setInvulnerable(structure.getInvulnerable());
        if (!structure.getTags().isEmpty())
        {
            entity.getScoreboardTags().clear();
            entity.getScoreboardTags().addAll(structure.getTags());
        }
        if (structure.getLastDamageCause() != null)
            entity.setLastDamageCause(new EntityDamageEvent(
                            entity,
                            structure.getLastDamageCause().getCause(),
                            structure.getLastDamageCause().getDamage()
                    )
            );
        if (entity instanceof Damageable)
        {
            if (structure.getMaxHealth() != null)
                // noinspection deprecation
                ((Damageable) entity).setMaxHealth(structure.getMaxHealth());
            if (structure.getHealth() != null)
                ((Damageable) entity).setHealth(structure.getHealth());
        }
        if (entity instanceof LivingEntity)
        {
            if (!structure.getPotionEffects().isEmpty())
            {
                new ArrayList<>(((LivingEntity) entity).getActivePotionEffects()).stream()
                        .map(PotionEffect::getType)
                        .forEach(((LivingEntity) entity)::removePotionEffect);

                structure.getPotionEffects().stream()
                        .map(b -> new PotionEffect(
                                        b.getType(),
                                        b.getDuration(),
                                        b.getAmplifier(),
                                        b.isAmbient(),
                                        b.hasParticles(),
                                        b.hasIcon()
                                )
                        )
                        .forEach(((LivingEntity) entity)::addPotionEffect);
            }
            if (structure.getFireTicks() != null)
                entity.setFireTicks(structure.getFireTicks());
            if (structure.getTicksLived() != null)
                entity.setTicksLived(structure.getTicksLived());
            if (structure.getPortalCooldown() != null)
                entity.setPortalCooldown(structure.getPortalCooldown());
            if (structure.getPersistent() != null)
            {
                entity.setPersistent(structure.getPersistent());
                if (structure.getFallDistance() != null)
                    entity.setFallDistance(structure.getFallDistance());
            }
        }
    }

    public static void applyBlockStructureData(@NotNull BlockStructure structure)
    {
        Location loc = structure.getLocation();
        if (loc == null)
            throw new IllegalArgumentException("Location is null");

        applyBlockStructureData(structure, loc);
    }

    public static Block applyBlockStructureData(@NotNull BlockStructure blockStructure, @Nullable Location location)
    {
        Location targetLoc = location == null ? blockStructure.getLocation(): location;
        if (targetLoc == null)
            return null;

        Block block = targetLoc.getBlock();
        if (blockStructure.getType() != null)
            block.setType(blockStructure.getType(), true);
        if (blockStructure.getBiome() != null)
            block.getWorld().setBiome(targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ(), blockStructure.getBiome());
        if (!blockStructure.getMetadata().isEmpty())
        {
            ClassLoader classLoader = StructureUtils.class.getClassLoader();
            if (!(classLoader instanceof PluginClassLoader))
                throw new IllegalStateException("ClassLoader is not PluginClassLoader");
            Plugin owningPlugin = ((PluginClassLoader) classLoader).getPlugin();

            for (Map.Entry<String, Object> entry : blockStructure.getMetadata().entrySet())
                block.setMetadata(
                        entry.getKey(),
                        new FixedMetadataValue(owningPlugin, entry.getValue())
                );
        }

        if (!blockStructure.getBlockData().isEmpty())
        {
            BlockData data = BlockDataParser.fromMap(block.getType(), blockStructure.getBlockData());
            block.setBlockData(block.getBlockData().merge(data), true);
        }

        if (blockStructure.getBlockState() != null)
        {
            // noinspection deprecation
            block.getState().getData().setData(blockStructure.getBlockState());
            block.getState().update(true, true);
        }

        return block;
    }

    public static Block applyBlockStructureData(@NotNull ScenarioEngine engine, @NotNull BlockStructure block, @Nullable Location location)
    {
        Location targetLoc;
        if (location != null)
            targetLoc = Utils.assignWorldToLocation(location, engine);
        else if (block.getLocation() != null)
            targetLoc = Utils.assignWorldToLocation(block.getLocation(), engine);
        else
            return null;

        return applyBlockStructureData(block, targetLoc);
    }

    public static Block applyBlockStructureData(@NotNull ScenarioEngine engine, @NotNull BlockStructure block)
    {
        return applyBlockStructureData(engine, block, null);
    }

    public static boolean isSame(@NotNull EntityStructure entityStructure, @NotNull Entity entity, boolean strict)
    {
        if (entityStructure.getType() != null)
            if (entity.getType() != entityStructure.getType())
                return false;
        if (entityStructure.getCustomName() != null)
            if (!Objects.equals(entity.getCustomName(), entityStructure.getCustomName()))
                return false;
        if (entityStructure.getVelocity() != null)
            if (!entity.getVelocity().equals(entityStructure.getVelocity()))
                return false;
        if (entityStructure.getCustomNameVisible() != null)
            if (entity.isCustomNameVisible() != entityStructure.getCustomNameVisible())
                return false;
        if (entityStructure.getGlowing() != null)
            if (entity.isGlowing() != entityStructure.getGlowing())
                return false;
        if (entityStructure.getGravity() != null)
            if (entity.hasGravity() != entityStructure.getGravity())
                return false;
        if (entityStructure.getSilent() != null)
            if (entity.isSilent() != entityStructure.getSilent())
                return false;
        if (entityStructure.getInvulnerable() != null)
            if (entity.isInvulnerable() != entityStructure.getInvulnerable())
                return false;
        if (entityStructure.getCustomNameVisible() != null)
            if (entity.isCustomNameVisible() != entityStructure.getCustomNameVisible())
                return false;
        if (entityStructure.getInvulnerable() != null)
            if (entity.isInvulnerable() != entityStructure.getInvulnerable())
                return false;
        if (!entityStructure.getTags().isEmpty())
        {
            ArrayList<String> tags = new ArrayList<>(entity.getScoreboardTags());
            if (strict && tags.size() != entityStructure.getTags().size())
                return false;
            if (!tags.containsAll(entityStructure.getTags()))
                return false;
        }

        if (entityStructure.getLastDamageCause() != null)
        {
            EntityDamageEvent lastDamageCause = entity.getLastDamageCause();
            if (lastDamageCause == null)
                return false;
            if (lastDamageCause.getCause() != entityStructure.getLastDamageCause().getCause())
                return false;
            if (lastDamageCause.getDamage() != entityStructure.getLastDamageCause().getDamage())
                return false;
        }

        if (entity instanceof Damageable)
        {
            if (entityStructure.getMaxHealth() != null)
                // noinspection deprecation
                if (((Damageable) entity).getMaxHealth() != entityStructure.getMaxHealth())
                    return false;
            if (entityStructure.getHealth() != null)
                if (((Damageable) entity).getHealth() != entityStructure.getHealth())
                    return false;
        }

        if (entity instanceof LivingEntity)
            if (!entityStructure.getPotionEffects().isEmpty())
            {
                List<PotionEffect> potionEffects = new ArrayList<>(((LivingEntity) entity).getActivePotionEffects());
                if (strict && potionEffects.size() != entityStructure.getPotionEffects().size())
                    return false;

                for (PotionEffect effects : entityStructure.getPotionEffects())
                    if (!potionEffects.contains(effects))
                        return false;
            }

        if (entityStructure.getFireTicks() != null)
            if (entity.getFireTicks() != entityStructure.getFireTicks())
                return false;
        if (entityStructure.getTicksLived() != null)
            if (entity.getTicksLived() != entityStructure.getTicksLived())
                return false;
        if (entityStructure.getPortalCooldown() != null)
            if (entity.getPortalCooldown() != entityStructure.getPortalCooldown())
                return false;
        if (entityStructure.getPersistent() != null)
            if (entity.isPersistent() != entityStructure.getPersistent())
                return false;

        if (entityStructure.getFallDistance() != null)
            // noinspection RedundantIfStatement
            if (entity.getFallDistance() != entityStructure.getFallDistance())
                return false;

        return true;
    }

    public static boolean isSame(@NotNull ProjectileStructure projectileStructure, @NotNull Projectile projectile, boolean isStrict)
    {
        if (projectile.getType() != projectileStructure.getType())
            return false;

        return isSame((EntityStructure) projectileStructure, projectile, isStrict)
                && (projectileStructure.getShooter() == null || (projectile.getShooter() != null && isSame(projectileStructure.getShooter(), (Entity) projectile.getShooter(), isStrict)));
    }

    public static void applyItemStructureData(EntityItemStructure structure, Entity dropper, Item entity)
    {
        EntityDropItemEvent event = new EntityDropItemEvent(dropper, entity);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
        {
            entity.remove();
            return;
        }

        if (structure.getPickupDelay() != null)
            entity.setPickupDelay(structure.getPickupDelay());
        if (structure.getOwner() != null)
            entity.setOwner(structure.getOwner());
        if (structure.getThrower() != null)
            entity.setThrower(structure.getThrower());
        if (structure.getVelocity() != null)
            entity.setVelocity(structure.getVelocity());
        if (structure.getCanMobPickup() != null)
            entity.setCanMobPickup(structure.getCanMobPickup());
        if (structure.getWillAge() != null)
            entity.setWillAge(structure.getWillAge());
    }
}
