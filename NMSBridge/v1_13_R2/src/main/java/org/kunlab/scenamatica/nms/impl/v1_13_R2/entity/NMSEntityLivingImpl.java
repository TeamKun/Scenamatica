package org.kunlab.scenamatica.nms.impl.v1_13_R2.entity;

import net.minecraft.server.v1_13_R2.EntityLiving;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_13_R2.TypeSupportImpl;
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
    public EntityLiving getNMSRaw()
    {
        return this.nmsEntity;
    }
}
