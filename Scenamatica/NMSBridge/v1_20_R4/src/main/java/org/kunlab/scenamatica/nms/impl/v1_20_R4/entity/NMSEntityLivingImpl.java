package org.kunlab.scenamatica.nms.impl.v1_20_R4.entity;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_20_R4.TypeSupportImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;

public class NMSEntityLivingImpl extends NMSEntityImpl implements NMSEntityLiving
{
    private final LivingEntity bukkitEntity;
    private final net.minecraft.world.entity.LivingEntity nmsEntity;

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
        this.nmsEntity.startUsingItem(TypeSupportImpl.toNMS(hand));
    }

    @Override
    public void broadcastItemBreak(NMSItemSlot slot)
    {
        this.nmsEntity.broadcastBreakEvent(TypeSupportImpl.toNMS(slot));
    }

    @Override
    public void receive(NMSEntity entity, int amount)
    {
        this.nmsEntity.take((Entity) entity.getNMSRaw(), amount);
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
        return this.nmsEntity.removeStingerTime;
    }

    @Override
    public void setArrowCooldown(int cooldown)
    {
        this.nmsEntity.removeStingerTime = cooldown;
    }

    @Override
    public net.minecraft.world.entity.LivingEntity getNMSRaw()
    {
        return this.nmsEntity;
    }
}
