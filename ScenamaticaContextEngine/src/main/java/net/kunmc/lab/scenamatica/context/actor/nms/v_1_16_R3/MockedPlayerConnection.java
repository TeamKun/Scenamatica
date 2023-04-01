package net.kunmc.lab.scenamatica.context.actor.nms.v_1_16_R3;

import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.events.actor.ActorMessageReceiveEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import net.minecraft.server.v1_16_R3.PacketPlayOutKeepAlive;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;

import java.io.IOException;
import java.lang.reflect.Field;

class MockedPlayerConnection extends PlayerConnection
{
    private static final Field fChatComponent; // Lnet/minecraft/server/v1_16_R3/PacketPlayOutChat;a:Lnet/minecraft/network/chat/IChatBaseComponent;

    static
    {
        try
        {
            fChatComponent = PacketPlayOutChat.class.getDeclaredField("a");
            fChatComponent.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    public MockedPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
    {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    private static ActorMessageReceiveEvent.Type getEventType(ChatMessageType type)
    {
        switch (type)
        {
            case CHAT:
                return ActorMessageReceiveEvent.Type.PLAYER;
            case GAME_INFO:
                return ActorMessageReceiveEvent.Type.GAME_INFO;
            case SYSTEM:
            default:
                return ActorMessageReceiveEvent.Type.SYSTEM;
        }
    }

    @Override
    public void sendPacket(Packet<?> packet)
    {
        if (packet instanceof PacketPlayOutKeepAlive)
            this.handleKeepAlive((PacketPlayOutKeepAlive) packet);
        else if (packet instanceof PacketPlayOutChat)
            this.handleChat((PacketPlayOutChat) packet);
        else
            super.sendPacket(packet);
    }

    @SneakyThrows(IOException.class)
    private void handleKeepAlive(PacketPlayOutKeepAlive packet)
    {
        PacketDataSerializer buf = new PacketDataSerializer(ByteBufAllocator.DEFAULT.buffer());
        packet.b(buf);
        PacketPlayInKeepAlive packetPlayInKeepAlive = new PacketPlayInKeepAlive();
        packetPlayInKeepAlive.a(buf);

        this.a(packetPlayInKeepAlive);
    }

    private void handleChat(PacketPlayOutChat packet)
    {
        ChatMessageType type = packet.d();
        BaseComponent[] components = packet.components;
        if (components == null)
        {
            try
            {
                IChatBaseComponent component = (IChatBaseComponent) fChatComponent.get(packet);
                components = new ComponentBuilder(CraftChatMessage.fromComponent(component)).create();
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }

            if (components[0] == null)
                return;
        }

        TextComponent textComponent = new TextComponent(components);

        ActorMessageReceiveEvent event = new ActorMessageReceiveEvent(
                this.player.getBukkitEntity(),
                textComponent,
                getEventType(type)
        );

        assert this.player.getMinecraftServer() != null;
        this.player.getMinecraftServer().server.getPluginManager().callEvent(event);
    }
}
