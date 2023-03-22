package net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3;

import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_16_R3.PacketPlayOutKeepAlive;
import net.minecraft.server.v1_16_R3.PlayerConnection;

import java.io.IOException;

class MockedPlayerConnection extends PlayerConnection
{
    public MockedPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
    {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    @SneakyThrows(IOException.class)
    public void sendPacket(Packet<?> packet)
    {
        if (packet instanceof PacketPlayOutKeepAlive)
        {
            PacketDataSerializer buf = new PacketDataSerializer(ByteBufAllocator.DEFAULT.buffer());
            packet.b(buf);
            PacketPlayInKeepAlive packetPlayInKeepAlive = new PacketPlayInKeepAlive();
            packetPlayInKeepAlive.a(buf);

            this.a(packetPlayInKeepAlive);
            return;
        }

        super.sendPacket(packet);
    }
}
