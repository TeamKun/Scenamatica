package org.kunlab.scenamatica.context.actor.nms.v1_13_R2;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import net.minecraft.server.v1_13_R2.EnumProtocol;
import net.minecraft.server.v1_13_R2.EnumProtocolDirection;
import net.minecraft.server.v1_13_R2.LegacyPingHandler;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.NetworkManager;
import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketDecoder;
import net.minecraft.server.v1_13_R2.PacketEncoder;
import net.minecraft.server.v1_13_R2.PacketPrepender;
import net.minecraft.server.v1_13_R2.PacketSplitter;
import net.minecraft.server.v1_13_R2.ServerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;

class MockedNetworkManager extends NetworkManager
{
    public static final String DEFAULT_IP = "10.48.51.114";
    public static final Integer DEFAULT_PORT = 1919;
    private static final Field fConnectedChannels;  // Lnet/minecraft/server/NetworkManager;f:Ljava/util/List<Lnet.minecraft.server.NetworkManager;>;

    static
    {
        try
        {
            fConnectedChannels = ServerConnection.class.getDeclaredField("g");
            fConnectedChannels.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean alive;

    @SneakyThrows(UnknownHostException.class)
    public MockedNetworkManager(MinecraftServer server, PlayerStructure initialStructure)
    {
        super(EnumProtocolDirection.SERVERBOUND);

        this.alive = true;

        InetAddress addr;
        InetSocketAddress socketAddr;
        if (initialStructure.getRemoteAddress() == null)
            addr = InetAddress.getByName(DEFAULT_IP);
        else
            addr = initialStructure.getRemoteAddress();
        if (initialStructure.getPort() == null)
            socketAddr = new InetSocketAddress(addr, DEFAULT_PORT);
        else
            socketAddr = new InetSocketAddress(addr, initialStructure.getPort());
        this.socketAddress = socketAddr;
        this.channel = new EmbeddedChannel();
        this.channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                .addLast("legacy_query", new LegacyPingHandler(((CraftServer) Bukkit.getServer()).getServer().getServerConnection()))
                .addLast("splitter", new PacketSplitter())
                .addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND))
                .addLast("prepender", new PacketPrepender())
                .addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND))
                .addLast("packet_handler", this);

        registerToMinecraft(server, this);
        this.preparing = false;  // ディスコネ時に, iterator から削除されるように。
    }

    private static void registerToMinecraft(MinecraftServer server, NetworkManager networkManager)
    {
        try
        {
            @SuppressWarnings("unchecked")
            List<NetworkManager> connectedChannels = (List<NetworkManager>) fConnectedChannels.get(server.getServerConnection());
            connectedChannels.add(networkManager);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    // これ以下は, パケットの送受信を握りつぶし, 余計な例外を抑制する。

    @Override
    public void setProtocol(EnumProtocol enumprotocol)
    {
    }

    @Override
    public void sendPacket(Packet<?> packet)
    {
    }

    @Override
    public void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericfuturelistener)
    {
    }

    @Override
    public void a(SecretKey secretkey)
    {
    }

    @Override
    public boolean isLocal()
    {
        return false;
    }

    @Override
    public boolean isConnected()
    {
        return this.alive;
    }

    @Override
    public void stopReading()
    {
    }

    @Override
    public void setCompressionLevel(int i)
    {
    }

    @Override
    public void handleDisconnection()
    {
        super.handleDisconnection();
        this.alive = false;
    }

    @Override
    public SocketAddress getRawAddress()
    {
        return this.socketAddress;
    }
}
