package org.kunlab.scenamatica.nms.impl.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;

public class NMSEntityPlayerImpl extends NMSEntityHumanImpl implements NMSEntityPlayer
{
    private final Player bukkitEntity;
    private final EntityPlayer nmsEntity;

    public NMSEntityPlayerImpl(Player bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftPlayer) bukkitEntity).getHandle();
    }

    @Override
    public Player getBukkit()
    {
        return this.bukkitEntity;
    }

    @Override
    public Object getNMSRaw()
    {
        return this.nmsEntity;
    }
}
