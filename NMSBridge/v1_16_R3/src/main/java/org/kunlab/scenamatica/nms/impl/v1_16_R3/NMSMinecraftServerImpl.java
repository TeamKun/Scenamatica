package org.kunlab.scenamatica.nms.impl.v1_16_R3;

import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.support.WorldMapImpl;
import org.kunlab.scenamatica.nms.supports.WorldMap;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;

public class NMSMinecraftServerImpl implements NMSMinecraftServer
{
    private final Server bukkitServer;
    private final MinecraftServer nmsServer;
    private final NMSPlayerListImpl playerList;

    public NMSMinecraftServerImpl(Server bukkitServer)
    {
        this.bukkitServer = bukkitServer;
        this.nmsServer = ((CraftServer) bukkitServer).getServer();
        this.playerList = new NMSPlayerListImpl(this.nmsServer.getPlayerList());
    }

    @Override
    public MinecraftServer getNMSRaw()
    {
        return this.nmsServer;
    }

    @Override
    public Server getBukkit()
    {
        return this.bukkitServer;
    }

    @Override
    public WorldMap getWorlds()
    {
        return new WorldMapImpl(this.nmsServer.worldServer);
    }
}
