package org.kunlab.scenamatica.nms.impl.v1_13_R2.entity;

import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityItem;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EnumMoveType;
import net.minecraft.server.v1_13_R2.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.impl.v1_13_R2.TypeSupportImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

import java.lang.reflect.Field;

public class NMSEntityImpl implements NMSEntity
{
    private static final Field fForceDrops; // Lnet/minecraft/server/<version>/EntityLiving;.forceDrops:Z

    static
    {
        try
        {
            fForceDrops = EntityLiving.class.getDeclaredField("forceDrops");
            fForceDrops.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private final org.bukkit.entity.Entity bukkitEntity;
    private final Entity nmsEntity;

    public NMSEntityImpl(org.bukkit.entity.Entity bukkitEntity)
    {
        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
    }

    private static void setForceDrops(EntityLiving entity, boolean forceDrops)
    {
        try
        {
            fForceDrops.set(entity, forceDrops);
        }
        catch (IllegalAccessException ignored)
        {
        }
    }

    private static boolean isForceDrops(EntityLiving entity)
    {
        try
        {
            return fForceDrops.getBoolean(entity);
        }
        catch (IllegalAccessException ignored)
        {
            return false;
        }
    }

    @Override
    public Entity getNMSRaw()
    {
        return this.nmsEntity;
    }

    @Override
    public org.bukkit.entity.Entity getBukkit()
    {
        return this.bukkitEntity;
    }

    @Override
    public void move(NMSMoveType moveType, Location location)
    {
        EnumMoveType convertedMoveType = TypeSupportImpl.toNMS(moveType);

        this.nmsEntity.move(
                convertedMoveType,
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    @Override
    public NMSEntityItem dropItem(@NotNull NMSItemStack stack, float offsetY)
    {
        boolean forceDrops = false;
        if (this.nmsEntity instanceof EntityLiving)
        {
            EntityLiving entityLiving = (EntityLiving) this.nmsEntity;
            forceDrops = isForceDrops(entityLiving);
            if (!forceDrops)
                setForceDrops(entityLiving, true);
        }

        EntityItem dropped = this.nmsEntity.a((ItemStack) stack.getNMSRaw(), offsetY);
        if (this.nmsEntity instanceof EntityLiving)
        {
            EntityLiving entityLiving = (EntityLiving) this.nmsEntity;
            setForceDrops(entityLiving, forceDrops);
        }

        if (dropped == null)
            return null;


        return new NMSEntityItemImpl(dropped);
    }

    @Override
    public boolean isInvisible()
    {
        return this.nmsEntity.isInvisible();
    }

    @Override
    public void setInvisible(boolean invisible)
    {
        this.nmsEntity.setInvisible(invisible);
    }
}
