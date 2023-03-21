package net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.SneakyThrows;
import net.minecraft.server.v1_16_R3.EnumProtocol;
import net.minecraft.server.v1_16_R3.EnumProtocolDirection;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

class MockedNetworkManager extends NetworkManager
{
    @SneakyThrows(UnknownHostException.class)
    public MockedNetworkManager()
    {
        super(EnumProtocolDirection.SERVERBOUND);

        this.socketAddress = new InetSocketAddress(InetAddress.getByName("191.9.81.0"), 1919);
        this.channel = null;  // 多分 null 許容
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
    public void close(IChatBaseComponent ichatbasecomponent)
    {
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext)
    {

    }

    @Override
    public void channelInactive(ChannelHandlerContext channelhandlercontext)
    {
    }

    @Override
    public boolean isLocal()
    {
        return false;
    }

    @Override
    public void a(Cipher cipher, Cipher cipher1)
    {
    }

    @Override
    public boolean isConnected()
    {
        return true;
    }

    @Override
    public boolean i()
    {
        return false;  // 親では, channel が null か判定しているので
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
    }

    @Override
    public SocketAddress getRawAddress()
    {
        return this.socketAddress;
    }
}
