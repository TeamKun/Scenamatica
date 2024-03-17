package org.kunlab.scenamatica.nms.v1_16_R3;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.WrapperProvider;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityHuman;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSEntityHumanImpl;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSEntityImpl;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSEntityLivingImpl;
import org.kunlab.scenamatica.nms.v1_16_R3.entity.NMSEntityPlayerImpl;

public class WrapperProviderImpl implements WrapperProvider
{
    public static NMSEntity wrap$(Entity bukkitEntity)
    {
        return new NMSEntityImpl(bukkitEntity);
    }

    public static NMSEntityLiving wrap$(LivingEntity bukkitEntity)
    {
        return new NMSEntityLivingImpl(bukkitEntity);
    }

    public static NMSEntityHuman wrap$(HumanEntity bukkitEntity)
    {
        return new NMSEntityHumanImpl(bukkitEntity);
    }

    public static NMSEntityPlayer wrap$(org.bukkit.entity.Player bukkitEntity)
    {
        return new NMSEntityPlayerImpl(bukkitEntity);
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
    public NMSEntityLiving wrap(LivingEntity bukkitEntity)
    {
        return wrap$(bukkitEntity);
    }

    @Override
    public NMSEntityHuman wrap(HumanEntity bukkitEntity)
    {
        return wrap$(bukkitEntity);
    }

    @Override
    public NMSEntityPlayer wrap(Player bukkitEntity)
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
