package org.kunlab.scenamatica.nms.impl.v1_18_R1.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.impl.v1_18_R1.TypeSupportImpl;
import org.kunlab.scenamatica.nms.impl.v1_18_R1.utils.DamageSourceSupport;
import org.kunlab.scenamatica.nms.impl.v1_18_R1.utils.NMSSupport;
import org.kunlab.scenamatica.nms.types.entity.NMSDamageSource;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

public class NMSEntityImpl implements NMSEntity
{
    private final org.bukkit.entity.Entity bukkitEntity;
    private final Entity nmsEntity;

    public NMSEntityImpl(org.bukkit.entity.Entity bukkitEntity)
    {
        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
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
        MoverType convertedMoveType = TypeSupportImpl.toNMS(moveType);
        Vec3 convertedLocation = NMSSupport.convertLocToVec3D(location);

        this.nmsEntity.move(convertedMoveType, convertedLocation);
    }

    @Override
    public NMSEntityItem dropItem(@NotNull NMSItemStack stack, float offsetY)
    {
        boolean forceDrops = false;
        if (this.nmsEntity instanceof LivingEntity)
        {
            LivingEntity entityLiving = (LivingEntity) this.nmsEntity;
            forceDrops = entityLiving.forceDrops;
            if (!forceDrops)
                entityLiving.forceDrops = true;
        }

        ItemEntity dropped = this.nmsEntity.spawnAtLocation((ItemStack) stack.getNMSRaw(), offsetY);
        if (this.nmsEntity instanceof LivingEntity)
        {
            LivingEntity entityLiving = (LivingEntity) this.nmsEntity;
            entityLiving.forceDrops = forceDrops;
        }

        if (dropped == null)
            return null;

        return new NMSEntityItemImpl(dropped);
    }

    @Override
    public boolean damageEntity(NMSDamageSource source, float damage)
    {
        return this.nmsEntity.hurt(
                DamageSourceSupport.fromNMSDamageSource(source),
                damage
        );
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
