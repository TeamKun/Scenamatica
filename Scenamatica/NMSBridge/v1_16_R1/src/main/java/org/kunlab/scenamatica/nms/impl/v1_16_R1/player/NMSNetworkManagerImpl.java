package org.kunlab.scenamatica.nms.impl.v1_16_R1.player;

import net.minecraft.server.v1_16_R1.NetworkManager;
import org.kunlab.scenamatica.nms.types.player.NMSNetworkManager;

import java.net.SocketAddress;

public class NMSNetworkManagerImpl implements NMSNetworkManager
{
    private final NetworkManager networkManager;

    public NMSNetworkManagerImpl(NetworkManager networkManager)
    {
        this.networkManager = networkManager;
    }

    @Override
    public NetworkManager getNMSRaw()
    {
        return this.networkManager;
    }

    @Override
    public SocketAddress getSocketAddress()
    {
        return this.networkManager.getSocketAddress();
    }
}
