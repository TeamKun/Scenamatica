package org.kunlab.scenamatica.nms.impl.v1_13_R2.utils;

import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityArrow;
import net.minecraft.server.v1_13_R2.EntityFireball;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityLiving;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSDamageSource;

public class DamageSourceSupport
{
    public static DamageSource fromNMSDamageSource(NMSDamageSource source)
    {
        switch (source.getDamageType())
        {
            case NMSDamageSource.ARROW_TYPE:
                return DamageSource.arrow(
                        convertEntity(source.getEntity(), EntityArrow.class, org.bukkit.entity.Arrow.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.CACTUS_TYPE:
                return DamageSource.CACTUS;
            case NMSDamageSource.CRAMMING_TYPE:
                return DamageSource.CRAMMING;
            case NMSDamageSource.DRAGON_BREATH_TYPE:
                return DamageSource.DRAGON_BREATH;
            case NMSDamageSource.DROWN_TYPE:
                return DamageSource.DROWN;
            case NMSDamageSource.DRYOUT_TYPE:
                return DamageSource.DRYOUT;
            case NMSDamageSource.EXPLOSION_TYPE:
            case NMSDamageSource.EXPLOSION_PLAYER_TYPE:
                return DamageSource.b(
                        convertEntity(source.getEntity(), EntityLiving.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.FALL_TYPE:
                return DamageSource.FALL;
            case NMSDamageSource.FALLING_BLOCK_TYPE:
                return DamageSource.FALLING_BLOCK;
            case NMSDamageSource.FIREBALL_TYPE:
                return DamageSource.fireball(
                        convertEntity(source.getEntity(), EntityFireball.class, org.bukkit.entity.Fireball.class),
                        convertEntity(source.getDirectEntity(), EntityLiving.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.FIREWORKS_TYPE:
                return DamageSource.FIREWORKS;
            case NMSDamageSource.FLY_INTO_WALL_TYPE:
                return DamageSource.FLY_INTO_WALL;
            case NMSDamageSource.GENERIC_TYPE:
                return DamageSource.GENERIC;
            case NMSDamageSource.HOT_FLOOR_TYPE:
                return DamageSource.HOT_FLOOR;
            case NMSDamageSource.IN_FIRE_TYPE:
                return DamageSource.FIRE;
            case NMSDamageSource.IN_WALL_TYPE:
                return DamageSource.STUCK;
            case NMSDamageSource.LAVA_TYPE:
                return DamageSource.LAVA;
            case NMSDamageSource.LIGHTNING_BOLT_TYPE:
                return DamageSource.LIGHTNING;
            case NMSDamageSource.MAGIC_TYPE:
                return DamageSource.MAGIC;
            case NMSDamageSource.MOB_TYPE:
                return DamageSource.mobAttack(
                        convertEntity(source.getEntity(), EntityLiving.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.ON_FIRE_TYPE:
                return DamageSource.BURN;
            case NMSDamageSource.OUT_OF_WORLD_TYPE:
                return DamageSource.OUT_OF_WORLD;
            case NMSDamageSource.PLAYER_TYPE:
                return DamageSource.playerAttack(
                        convertEntity(source.getEntity(), EntityHuman.class, org.bukkit.entity.Player.class)
                );
            case NMSDamageSource.STARVE_TYPE:
                return DamageSource.STARVE;
            case NMSDamageSource.THORNS_TYPE:
                return DamageSource.a(convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.LivingEntity.class));
            case NMSDamageSource.INDIRECT_MAGIC_TYPE:
                return DamageSource.c(
                        convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.Entity.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.THROWN_TYPE:
                return DamageSource.projectile(
                        convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.Entity.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.TRIDENT_TYPE:
                return DamageSource.a(
                        convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.Trident.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.WITHER_TYPE:
                return DamageSource.WITHER;
            default:
                throw new IllegalArgumentException("Unknown damage type: " + source.getDamageType());
        }
    }

    private static <T extends Entity> T convertEntity(org.bukkit.entity.Entity entity, Class<T> nmsClazz, Class<? extends org.bukkit.entity.Entity> bukkitClazz)
    {
        if (!bukkitClazz.isInstance(entity))
            throw new IllegalArgumentException("Entity is not an instance of " + bukkitClazz.getName());
        return nmsClazz.cast(((CraftEntity) entity).getHandle());
    }
}
