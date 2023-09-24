package org.kunlab.scenamatica.nms.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.EntityLiving;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSLivingEntity;

public class NMSLivingEntityImpl extends NMSEntityImpl implements NMSLivingEntity
{
    private final LivingEntity bukkitEntity;
    private final EntityLiving nmsEntity;

    public NMSLivingEntityImpl(LivingEntity bukkitEntity)
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
}
