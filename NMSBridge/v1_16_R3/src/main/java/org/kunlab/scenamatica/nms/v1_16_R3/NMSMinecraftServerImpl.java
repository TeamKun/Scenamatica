package org.kunlab.scenamatica.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.kunlab.scenamatica.nms.supports.WorldMap;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.v1_16_R3.support.WorldMapImpl;

public class NMSMinecraftServerImpl implements NMSMinecraftServer
{
    private final Server bukkitServer;
    private final MinecraftServer nmsServer;

    public NMSMinecraftServerImpl(Server bukkitServer)
    {
        this.bukkitServer = bukkitServer;
        this.nmsServer = ((CraftServer) bukkitServer).getServer();
    }

    @Override
    public Object getNMSRaw()
    {
        return this.nmsServer;
    }

    @Override
    public Object getNMSCraftRaw()
    {
        return this.bukkitServer;
    }

    @Override
    public WorldMap getWorlds()
    {
        return new WorldMapImpl(this.nmsServer.worldServer);
    }
}
