package org.kunlab.scenamatica.nms.impl.v1_18_R1.player;

import net.minecraft.network.Connection;
import org.kunlab.scenamatica.nms.types.player.NMSNetworkManager;

import java.net.SocketAddress;

public class NMSNetworkManagerImpl implements NMSNetworkManager
{
    private final Connection networkManager;

    public NMSNetworkManagerImpl(Connection networkManager)
    {
        this.networkManager = networkManager;
    }

    @Override
    public Connection getNMSRaw()
    {
        return this.networkManager;
    }

    @Override
    public SocketAddress getSocketAddress()
    {
        return this.networkManager.address;
    }
}
