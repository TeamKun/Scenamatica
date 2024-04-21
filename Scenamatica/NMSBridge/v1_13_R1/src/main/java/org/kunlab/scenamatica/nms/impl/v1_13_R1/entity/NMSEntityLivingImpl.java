package org.kunlab.scenamatica.nms.impl.v1_13_R1.entity;

import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityLiving;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.exceptions.UnsupportedNMSOperationException;
import org.kunlab.scenamatica.nms.impl.v1_13_R1.TypeSupportImpl;
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

    private static int getAnimationID(NMSItemSlot itemslot)
    {
        int base = 47;
        switch (itemslot)
        {
            case OFFHAND:
                return base + 1;
            case FEET:
                return base + 5;
            case LEGS:
                return base + 4;
            case CHEST:
                return base + 3;
            case HEAD:
                return base + 2;
            case MAINHAND:
            default:
                return base;
        }
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
        this.nmsEntity.world.broadcastEntityEffect(
                this.nmsEntity,
                (byte) getAnimationID(slot)
        );
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
        throw UnsupportedNMSOperationException.of(
                this.getClass(),
                "getArrowCooldown",
                int.class
        );
    }

    @Override
    public void setArrowCooldown(int cooldown)
    {
        throw UnsupportedNMSOperationException.ofVoid(
                this.getClass(),
                "setArrowCooldown",
                int.class
        );
    }

    @Override
    public EntityLiving getNMSRaw()
    {
        return this.nmsEntity;
    }
}
