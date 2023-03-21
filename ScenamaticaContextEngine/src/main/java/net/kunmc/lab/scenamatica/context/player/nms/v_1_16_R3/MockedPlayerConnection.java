package net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_16_R3.PlayerConnection;

class MockedPlayerConnection extends PlayerConnection
{
    public MockedPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
    {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void tick()
    {
        this.a(new PacketPlayInKeepAlive());  // タイムアウト対策
    }
}
