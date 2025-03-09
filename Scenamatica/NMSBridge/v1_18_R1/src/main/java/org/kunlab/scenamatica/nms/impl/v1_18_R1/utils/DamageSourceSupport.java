package org.kunlab.scenamatica.nms.impl.v1_18_R1.utils;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSDamageSource;

public class DamageSourceSupport
{
    public static DamageSource fromNMSDamageSource(NMSDamageSource source)
    {
        switch (source.getDamageType())
        {
            case NMSDamageSource.ARROW_TYPE:
                return DamageSource.arrow(
                        convertEntity(source.getEntity(), Arrow.class, org.bukkit.entity.Arrow.class),
                        convertEntity(source.getDirectEntity(), LivingEntity.class, org.bukkit.entity.LivingEntity.class)
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
                return DamageSource.DRY_OUT;
            case NMSDamageSource.EXPLOSION_TYPE:
            case NMSDamageSource.EXPLOSION_PLAYER_TYPE:
                return DamageSource.explosion(
                        convertEntity(source.getEntity(), LivingEntity.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.FALL_TYPE:
                return DamageSource.FALL;
            case NMSDamageSource.FALLING_BLOCK_TYPE:
                return DamageSource.FALLING_BLOCK;
            case NMSDamageSource.FIREBALL_TYPE:
                return DamageSource.fireball(
                        convertEntity(source.getEntity(), Fireball.class, org.bukkit.entity.Fireball.class),
                        convertEntity(source.getDirectEntity(), LivingEntity.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.FIREWORKS_TYPE:
                return DamageSource.fireworks(
                        convertEntity(source.getEntity(), FireworkRocketEntity.class, org.bukkit.entity.Firework.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.FLY_INTO_WALL_TYPE:
                return DamageSource.FLY_INTO_WALL;
            case NMSDamageSource.FREEZE_TYPE:
                return DamageSource.FREEZE;
            case NMSDamageSource.GENERIC_TYPE:
                return DamageSource.GENERIC;
            case NMSDamageSource.HOT_FLOOR_TYPE:
                return DamageSource.HOT_FLOOR;
            case NMSDamageSource.IN_FIRE_TYPE:
                return DamageSource.IN_FIRE;
            case NMSDamageSource.IN_WALL_TYPE:
                return DamageSource.IN_WALL;
            case NMSDamageSource.LAVA_TYPE:
                return DamageSource.LAVA;
            case NMSDamageSource.LIGHTNING_BOLT_TYPE:
                return DamageSource.LIGHTNING_BOLT;
            case NMSDamageSource.MAGIC_TYPE:
                return DamageSource.MAGIC;
            case NMSDamageSource.MOB_TYPE:
                return DamageSource.mobAttack(
                        convertEntity(source.getEntity(), LivingEntity.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.ON_FIRE_TYPE:
                return DamageSource.ON_FIRE;
            case NMSDamageSource.OUT_OF_WORLD_TYPE:
                return DamageSource.OUT_OF_WORLD;
            case NMSDamageSource.PLAYER_TYPE:
                return DamageSource.playerAttack(
                        convertEntity(source.getEntity(), Player.class, org.bukkit.entity.Player.class)
                );
            case NMSDamageSource.STARVE_TYPE:
                return DamageSource.STARVE;
            case NMSDamageSource.STING_TYPE:
                return DamageSource.sting(
                        convertEntity(source.getEntity(), LivingEntity.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.STALAGMITE_TYPE:
                return DamageSource.STALAGMITE;
            case NMSDamageSource.SWEET_BERRY_BUSH_TYPE:
                return DamageSource.SWEET_BERRY_BUSH;
            case NMSDamageSource.THORNS_TYPE:
                return DamageSource.thorns(convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.LivingEntity.class));
            case NMSDamageSource.INDIRECT_MAGIC_TYPE:
                return DamageSource.indirectMagic(
                        convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.Entity.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.THROWN_TYPE:
                return DamageSource.thrown(
                        convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.Entity.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.TRIDENT_TYPE:
                return DamageSource.trident(
                        convertEntity(source.getEntity(), Entity.class, org.bukkit.entity.Trident.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.WITHER_TYPE:
                return DamageSource.WITHER;
            case NMSDamageSource.WITHER_SKULL_TYPE:
                return DamageSource.witherSkull(
                        convertEntity(source.getEntity(), WitherSkull.class, org.bukkit.entity.WitherSkull.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
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
