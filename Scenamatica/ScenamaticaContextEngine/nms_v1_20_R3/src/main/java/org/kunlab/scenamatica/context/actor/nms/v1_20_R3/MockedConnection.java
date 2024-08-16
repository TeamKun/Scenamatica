package org.kunlab.scenamatica.context.actor.nms.v1_20_R3;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.kunlab.scenamatica.events.actor.ActorMessageReceiveEvent;

class MockedConnection extends Connection
{
    private final MockedPlayer player;
    private final MinecraftServer server;
    @Getter
    private int windowStateId;

    public MockedConnection(MockedPlayer player, MinecraftServer server)
    {
        super(PacketFlow.CLIENTBOUND);
        this.player = player;
        this.server = server;
    }

    @Override
    public void send(Packet<?> packet)
    {
        if (packet instanceof ClientboundKeepAlivePacket)
            this.handleKeepAlive((ClientboundKeepAlivePacket) packet);
        else if (packet instanceof ClientboundPlayerChatPacket)
            this.handleChat((ClientboundPlayerChatPacket) packet);
        else if (packet instanceof ClientboundContainerSetContentPacket)
            this.handleSetSlot((ClientboundContainerSetContentPacket) packet);
        else if (packet instanceof ClientboundContainerSetSlotPacket)
            this.handleWindowItem((ClientboundContainerSetSlotPacket) packet);
        else
            super.send(packet);
    }

    private void handleSetSlot(ClientboundContainerSetContentPacket packet)
    {
        this.windowStateId = packet.getStateId();
    }

    private void handleWindowItem(ClientboundContainerSetSlotPacket packet)
    {
        this.windowStateId = packet.getStateId();
    }
    private void handleKeepAlive(ClientboundKeepAlivePacket packet)
    {
        long validationNumber = packet.getId();
        ServerboundKeepAlivePacket packetPlayInKeepAlive = new ServerboundKeepAlivePacket(validationNumber);

        this.player.connection.handleKeepAlive(packetPlayInKeepAlive);
    }

    private void handleChat(ClientboundPlayerChatPacket packet)
    {
        String content = packet.body().content();

        ActorMessageReceiveEvent event = new ActorMessageReceiveEvent(
                this.player.getBukkitEntity(),
                Text.of(content),
                ActorMessageReceiveEvent.Type.PLAYER
        );

        if (Bukkit.getServer().isPrimaryThread())
            Bukkit.getServer().getPluginManager().callEvent(event);
        else
            this.server.execute(() -> Bukkit.getServer().getPluginManager().callEvent(event));

    }

    @Override
    public void disconnect(Component component)
    {
        this.player.getManager().onDestroyActor(this.player);

        super.disconnect(component);
    }
}
