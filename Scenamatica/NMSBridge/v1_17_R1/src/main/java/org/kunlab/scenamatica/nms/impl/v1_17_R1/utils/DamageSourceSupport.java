package org.kunlab.scenamatica.nms.impl.v1_17_R1.utils;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityFireballFireball;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.entity.projectile.EntityWitherSkull;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.WitherSkull;
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
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.CACTUS_TYPE:
                return DamageSource.j;
            case NMSDamageSource.CRAMMING_TYPE:
                return DamageSource.g;
            case NMSDamageSource.DRAGON_BREATH_TYPE:
                return DamageSource.s;
            case NMSDamageSource.DROWN_TYPE:
                return DamageSource.h;
            case NMSDamageSource.DRYOUT_TYPE:
                return DamageSource.t;
            case NMSDamageSource.EXPLOSION_TYPE:
            case NMSDamageSource.EXPLOSION_PLAYER_TYPE:
                return DamageSource.d(
                        convertEntity(source.getEntity(), EntityLiving.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.FALL_TYPE:
                return DamageSource.k;
            case NMSDamageSource.FALLING_BLOCK_TYPE:
                return DamageSource.r;
            case NMSDamageSource.FIREBALL_TYPE:
                return DamageSource.fireball(
                        convertEntity(source.getEntity(), EntityFireballFireball.class, org.bukkit.entity.Fireball.class),
                        convertEntity(source.getDirectEntity(), EntityLiving.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.FIREWORKS_TYPE:
                return DamageSource.a(
                        convertEntity(source.getEntity(), EntityFireworks.class, org.bukkit.entity.Firework.class),
                        convertEntity(source.getDirectEntity(), Entity.class, org.bukkit.entity.Entity.class)
                );
            case NMSDamageSource.FLY_INTO_WALL_TYPE:
                return DamageSource.l;
            case NMSDamageSource.FREEZE_TYPE:
                return DamageSource.v;
            case NMSDamageSource.GENERIC_TYPE:
                return DamageSource.n;
            case NMSDamageSource.HOT_FLOOR_TYPE:
                return DamageSource.e;
            case NMSDamageSource.IN_FIRE_TYPE:
                return DamageSource.a;
            case NMSDamageSource.IN_WALL_TYPE:
                return DamageSource.f;
            case NMSDamageSource.LAVA_TYPE:
                return DamageSource.d;
            case NMSDamageSource.LIGHTNING_BOLT_TYPE:
                return DamageSource.b;
            case NMSDamageSource.MAGIC_TYPE:
                return DamageSource.o;
            case NMSDamageSource.MOB_TYPE:
                return DamageSource.mobAttack(
                        convertEntity(source.getEntity(), EntityLiving.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.ON_FIRE_TYPE:
                return DamageSource.c;
            case NMSDamageSource.OUT_OF_WORLD_TYPE:
                return DamageSource.m;
            case NMSDamageSource.PLAYER_TYPE:
                return DamageSource.playerAttack(
                        convertEntity(source.getEntity(), EntityHuman.class, org.bukkit.entity.Player.class)
                );
            case NMSDamageSource.STARVE_TYPE:
                return DamageSource.i;
            case NMSDamageSource.STING_TYPE:
                return DamageSource.b(
                        convertEntity(source.getEntity(), EntityLiving.class, org.bukkit.entity.LivingEntity.class)
                );
            case NMSDamageSource.STALAGMITE_TYPE:
                return DamageSource.x;
            case NMSDamageSource.SWEET_BERRY_BUSH_TYPE:
                return DamageSource.u;
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
                return DamageSource.p;
            case NMSDamageSource.WITHER_SKULL_TYPE:
                return DamageSource.a(
                        convertEntity(source.getEntity(), EntityWitherSkull.class, WitherSkull.class),
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
