package org.kunlab.scenamatica.nms.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.EntityLiving;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.enums.entity.NMSHand;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.v1_16_R3.TypeSupportImpl;

public class NMSEntityLivingImpl extends NMSEntityImpl implements NMSEntityLiving
{
    private final LivingEntity bukkitEntity;
    private final EntityLiving nmsEntity;

    public NMSEntityLivingImpl(LivingEntity bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = (EntityLiving) this.getNMSRaw();
    }

    @Override
    public LivingEntity getBukkitEntity()
    {
        return this.bukkitEntity;
    }

    @Override
    public void consume(NMSHand hand)
    {
        this.nmsEntity.c(TypeSupportImpl.toNMS(hand));
    }

    @Override
    public Object getNMSCraftRaw()
    {
        return this.nmsEntity;
    }


}
