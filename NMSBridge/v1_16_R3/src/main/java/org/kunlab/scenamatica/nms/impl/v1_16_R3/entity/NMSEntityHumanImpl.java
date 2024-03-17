package org.kunlab.scenamatica.nms.impl.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.EntityHuman;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityHuman;

public class NMSEntityHumanImpl extends NMSEntityLivingImpl implements NMSEntityHuman
{
    private final HumanEntity bukkitEntity;
    private final EntityHuman nmsEntity;

    public NMSEntityHumanImpl(HumanEntity bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftHumanEntity) bukkitEntity).getHandle();
    }

    @Override
    public HumanEntity getBukkit()
    {
        return this.bukkitEntity;
    }

    @Override
    public Object getNMSRaw()
    {
        return this.nmsEntity;
    }
}
