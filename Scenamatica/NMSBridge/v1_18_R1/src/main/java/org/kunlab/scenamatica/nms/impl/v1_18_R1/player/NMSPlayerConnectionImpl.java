package org.kunlab.scenamatica.nms.impl.v1_18_R1.player;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerConnection;

public class NMSPlayerConnectionImpl implements NMSPlayerConnection
{
    private final ServerGamePacketListenerImpl playerConnection;

    public NMSPlayerConnectionImpl(ServerGamePacketListenerImpl playerConnection)
    {
        this.playerConnection = playerConnection;
    }

    @Override
    public Object getNMSRaw()
    {
        return this.playerConnection;
    }

    @Override
    public void disconnect(String reason)
    {
        this.playerConnection.disconnect(reason);
    }
}
