package org.kunlab.scenamatica.nms.impl.v1_16_R3.player;

import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerConnection;

public class NMSPlayerConnectionImpl implements NMSPlayerConnection
{
    private final PlayerConnection playerConnection;

    public NMSPlayerConnectionImpl(PlayerConnection playerConnection)
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
