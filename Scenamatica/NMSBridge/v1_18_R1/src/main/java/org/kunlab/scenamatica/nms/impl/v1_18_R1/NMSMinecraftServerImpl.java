package org.kunlab.scenamatica.nms.impl.v1_18_R1;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.NMSPlayerList;

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
    public NMSPlayerList getPlayerList()
    {
        return this.playerList;
    }
}
