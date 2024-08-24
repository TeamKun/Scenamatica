package org.kunlab.scenamatica.context.actor.nms.v1_18_R1;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.kunlab.scenamatica.events.actor.ActorMessageReceiveEvent;

class MockedPlayerConnection extends Connection
{
    private final MinecraftServer server;
    private final MockedPlayer player;
    @Getter
    private int windowStateId;

    public MockedPlayerConnection(MinecraftServer server, MockedPlayer player)
    {
        super(PacketFlow.SERVERBOUND);
        this.server = server;
        this.player = player;
    }

    private static ActorMessageReceiveEvent.Type getEventType(ChatType type)
    {
        switch (type)
        {
            case CHAT:
                return ActorMessageReceiveEvent.Type.PLAYER;
            case GAME_INFO:
                return ActorMessageReceiveEvent.Type.GAME_INFO;
            case SYSTEM:
                /* fall through */
            default:
                return ActorMessageReceiveEvent.Type.SYSTEM;
        }
    }

    @Override
    public void send(Packet<?> packet)
    {
        if (packet instanceof ClientboundKeepAlivePacket)
            this.handleKeepAlive((ClientboundKeepAlivePacket) packet);
        else if (packet instanceof ClientboundChatPacket)
            this.handleChat((ClientboundChatPacket) packet);
        else if (packet instanceof ClientboundSetCarriedItemPacket)
            this.handleHeldItemSlot();
        else if (packet instanceof ClientboundContainerSetSlotPacket)
            this.handleSetSlot((ClientboundContainerSetSlotPacket) packet);
        else if (packet instanceof ClientboundContainerSetContentPacket)
            this.handleWindowItem((ClientboundContainerSetContentPacket) packet);
        else
            super.send(packet);
    }

    private void handleSetSlot(ClientboundContainerSetSlotPacket packet)
    {
        this.windowStateId = packet.getStateId();
    }

    private void handleWindowItem(ClientboundContainerSetContentPacket packet)
    {
        this.windowStateId = packet.getStateId();
    }

    private void handleHeldItemSlot()
    {
        int slot = this.player.getInventory().selected;
        PlayerItemHeldEvent event = new PlayerItemHeldEvent(
                this.player.getBukkitEntity(),
                slot,
                slot
        );

        Bukkit.getPluginManager().callEvent(event);
    }

    private void handleKeepAlive(ClientboundKeepAlivePacket packet)
    {
        long validationNumber = packet.getId();
        ServerboundKeepAlivePacket packetPlayInKeepAlive = new ServerboundKeepAlivePacket(validationNumber);

        this.player.connection.handleKeepAlive(packetPlayInKeepAlive);
    }

    private void handleChat(ClientboundChatPacket packet)
    {
        ChatType type = packet.getType();
        Component message = packet.getMessage();
        ActorMessageReceiveEvent event = new ActorMessageReceiveEvent(
                this.player.getBukkitEntity(),
                Text.of(message.getContents()),
                getEventType(type)
        );

        if (Bukkit.getServer().isPrimaryThread())
            Bukkit.getServer().getPluginManager().callEvent(event);
        else
            this.server.execute(() ->
                    Bukkit.getServer().getPluginManager().callEvent(event)
            );
    }

    @Override
    public void disconnect(Component s)
    {
        this.player.getManager().onDestroyActor(this.player);

        super.disconnect(s);
    }
}
