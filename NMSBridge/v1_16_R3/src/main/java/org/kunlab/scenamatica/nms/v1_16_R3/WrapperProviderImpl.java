package org.kunlab.scenamatica.nms.v1_16_R3;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.WrapperProvider;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSLivingEntity;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSEntityImpl;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSLivingEntityImpl;

public class WrapperProviderImpl implements WrapperProvider
{
    public static NMSEntity wrap$(Entity bukkitEntity)
    {
        return new NMSEntityImpl(bukkitEntity);
    }

    public static NMSLivingEntity wrap$(LivingEntity bukkitEntity)
    {
        return new NMSLivingEntityImpl(bukkitEntity);
    }

    public static NMSWorldServer wrap$(World bukkitWorld)
    {
        return new NMSWorldServerImpl(bukkitWorld);
    }

    public static NMSMinecraftServer wrap$(Server server)
    {
        return new NMSMinecraftServerImpl(server);
    }

    @Override
    public NMSEntity wrap(Entity bukkitEntity)
    {
        return wrap$(bukkitEntity);
    }

    @Override
    public NMSLivingEntity wrap(LivingEntity bukkitEntity)
    {
        return wrap$(bukkitEntity);
    }

    @Override
    public NMSWorldServer wrap(World bukkitWorld)
    {
        return wrap$(bukkitWorld);
    }

    @Override
    public NMSMinecraftServer wrap(Server server)
    {
        return wrap$(server);
    }
}
