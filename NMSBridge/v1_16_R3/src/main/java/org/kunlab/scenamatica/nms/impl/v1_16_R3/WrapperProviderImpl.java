package org.kunlab.scenamatica.nms.impl.v1_16_R3;

import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.kunlab.scenamatica.nms.WrapperProvider;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.block.NMSBlockPositionImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.entity.NMSEntityHumanImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.entity.NMSEntityImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.entity.NMSEntityLivingImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.entity.NMSEntityPlayerImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.item.NMSItemStackImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.player.NMSPlayerInteractManagerImpl;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;
import org.kunlab.scenamatica.nms.types.block.NMSBlockPosition;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityHuman;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

public class WrapperProviderImpl implements WrapperProvider
{
    public static NMSBlockPosition wrap$(Location bukkitLocation)
    {
        return new NMSBlockPositionImpl(bukkitLocation);
    }

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

    public static NMSEntityPlayer wrap$(Player bukkitEntity)
    {
        return new NMSEntityPlayerImpl(bukkitEntity);
    }

    public static NMSItemStack wrap$(ItemStack bukkitItemStack)
    {
        return new NMSItemStackImpl(bukkitItemStack);
    }

    public static NMSPlayerInteractManager wrap$(PlayerInteractManager playerInteractManager)
    {
        return new NMSPlayerInteractManagerImpl(playerInteractManager);
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
    public NMSItemStack wrap(ItemStack bukkitItemStack)
    {
        return wrap$(bukkitItemStack);
    }

    @Override
    public NMSWorldServer wrap(World bukkitWorld)
    {
        return wrap$(bukkitWorld);
    }

    @Override
    public NMSBlockPosition wrap(Location bukkitLocation)
    {
        return wrap$(bukkitLocation);
    }

    @Override
    public NMSMinecraftServer wrap(Server server)
    {
        return wrap$(server);
    }
}
