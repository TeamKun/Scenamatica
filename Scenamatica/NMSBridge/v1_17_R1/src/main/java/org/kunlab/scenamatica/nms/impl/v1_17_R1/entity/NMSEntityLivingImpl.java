package org.kunlab.scenamatica.nms.impl.v1_17_R1.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.TypeSupportImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;

public class NMSEntityLivingImpl extends NMSEntityImpl implements NMSEntityLiving
{
    private final LivingEntity bukkitEntity;
    private final EntityLiving nmsEntity;

    public NMSEntityLivingImpl(LivingEntity bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftLivingEntity) bukkitEntity).getHandle();
    }

    @Override
    public LivingEntity getBukkit()
    {
        return this.bukkitEntity;
    }

    @Override
    public void consume(NMSHand hand)
    {
        this.nmsEntity.c(TypeSupportImpl.toNMS(hand));
    }

    @Override
    public void broadcastItemBreak(NMSItemSlot slot)
    {
        this.nmsEntity.broadcastItemBreak(TypeSupportImpl.toNMS(slot));
    }

    @Override
    public void receive(NMSEntity entity, int amount)
    {
        this.nmsEntity.receive((Entity) entity.getNMSRaw(), amount);
    }

    @Override
    public boolean isSleeping()
    {
        return this.nmsEntity.isSleeping();
    }

    @Override
    public int getArrowCount()
    {
        return this.nmsEntity.getArrowCount();
    }

    @Override
    public void setArrowCount(int count)
    {
        this.nmsEntity.setArrowCount(count);
    }

    @Override
    public int getArrowCooldown()
    {
        return this.nmsEntity.aI;
    }

    @Override
    public void setArrowCooldown(int cooldown)
    {
        this.nmsEntity.aI = cooldown;
    }

    @Override
    public EntityLiving getNMSRaw()
    {
        return this.nmsEntity;
    }
}
