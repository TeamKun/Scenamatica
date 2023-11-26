package org.kunlab.scenamatica.commons.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class StructureUtils
{
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
