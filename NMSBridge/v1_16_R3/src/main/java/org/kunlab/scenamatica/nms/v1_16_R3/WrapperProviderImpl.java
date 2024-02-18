package org.kunlab.scenamatica.nms.v1_16_R3;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.WrapperProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSLivingEntity;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSEntityImpl;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSLivingEntityImpl;

public class WrapperProviderImpl implements WrapperProvider
{
    @Override
    public NMSEntity ofEntity(Entity bukkitEntity)
    {
        return new NMSEntityImpl(bukkitEntity);
    }

    @Override
    public NMSLivingEntity ofEntity(LivingEntity bukkitEntity)
    {
        return new NMSLivingEntityImpl(bukkitEntity);
    }
}
