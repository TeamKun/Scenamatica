package net.kunmc.lab.scenamatica.context.player;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.DefaultAttributeMap;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class MockedChannel implements Channel
{
    @Override
    public ChannelId id()
    {
        return null;
    }

    @Override
    public EventLoop eventLoop()
    {
        return null;
    }

    @Override
    public Channel parent()
    {
        return null;
    }

    @Override
    public ChannelConfig config()
    {
        return null;
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @Override
    public boolean isRegistered()
    {
        return true;
    }

    @Override
    public boolean isActive()
    {
        return true;
    }

    @Override
    public ChannelMetadata metadata()
    {
        return null;
    }

    @Override
    @SneakyThrows(UnknownHostException.class)
    public SocketAddress localAddress()
    {
        return new InetSocketAddress(InetAddress.getByName("191.9.81.0"), 1919);
    }

    @Override
    @SneakyThrows(UnknownHostException.class)
    public SocketAddress remoteAddress()
    {
        return new InetSocketAddress(InetAddress.getByName("191.9.81.0"), 1919);
    }

    @Override
    public ChannelFuture closeFuture()
    {
        return null;
    }

    @Override
    public boolean isWritable()
    {
        return true;
    }

    @Override
    public long bytesBeforeUnwritable()
    {
        return 0;
    }

    @Override
    public long bytesBeforeWritable()
    {
        return 0;
    }

    @Override
    public Unsafe unsafe()
    {
        return null;
    }

    @Override
    public ChannelPipeline pipeline()
    {
        return null;
    }

    @Override
    public ByteBufAllocator alloc()
    {
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress)
    {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress)
    {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1)
    {
        return null;
    }

    @Override
    public ChannelFuture disconnect()
    {
        return null;
    }

    @Override
    public ChannelFuture close()
    {
        return null;
    }

    @Override
    public ChannelFuture deregister()
    {
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public ChannelFuture close(ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public ChannelFuture deregister(ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public Channel read()
    {
        return this;
    }

    @Override
    public ChannelFuture write(Object o)
    {
        return null;
    }

    @Override
    public ChannelFuture write(Object o, ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public Channel flush()
    {
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise)
    {
        return null;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o)
    {
        return null;
    }

    @Override
    public ChannelPromise newPromise()
    {
        return null;
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise()
    {
        return null;
    }

    @Override
    public ChannelFuture newSucceededFuture()
    {
        return null;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable throwable)
    {
        return null;
    }

    @Override
    public ChannelPromise voidPromise()
    {
        return null;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> attributeKey)
    {
        return new DefaultAttributeMap().attr(attributeKey);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> attributeKey)
    {
        return true;
    }

    @Override
    public int compareTo(@NotNull Channel o)
    {
        return 0;
    }
}
