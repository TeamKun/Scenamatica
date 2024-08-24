package org.kunlab.scenamatica.context.actor.nms.v1_18_R1;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.context.actor.PlayerMockerBase;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

import java.net.InetSocketAddress;

public class PlayerMocker extends PlayerMockerBase
{
    private final ActorManager manager;

    public PlayerMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        super(registry, manager, registry.getEnvironment().getActorSettings());

        this.manager = manager;
    }

    private static void activateChannel(Connection conn)
    {
        conn.channel = new MockedChannel();
        conn.address = new InetSocketAddress("10.48.51.114", 1919);
        conn.preparing = false;
    }

    private static void injectServerConnection(Connection conn)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerConnectionListener serverConn = server.getConnection();
        assert serverConn != null;

        serverConn.getConnections().add(conn);
    }

    @Override
    protected void sendSettings(@NotNull Player player)
    {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        String locale = player.getLocale();
        int viewDistance = 0x02;
        ChatVisiblity chatMode = ChatVisiblity.FULL;
        boolean chatColors = true;
        int displayedSkinParts = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        // 0x01: cape, 0x02: jacket, 0x04: left sleeve, 0x08: right sleeve,
        // 0x10: left pants leg, 0x20: right pants leg, 0x40: hat
        HumanoidArm mainHand = entityPlayer.getMainArm();
        boolean disableTextFiltering = true;

        ServerboundClientInformationPacket packet = new ServerboundClientInformationPacket(
                locale,
                viewDistance,
                chatMode,
                chatColors,
                displayedSkinParts,
                mainHand,
                disableTextFiltering,
                true
        );
        entityPlayer.updateOptions(packet);
    }

    @Override
    protected Actor createActorInstance(@NotNull World world, @NotNull PlayerStructure structure)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = createGameProfile(structure);

        Location initialLocation = createInitialLocation(world, structure);
        return new MockedPlayer(
                this.manager,
                this,
                server,
                worldServer,
                profile,
                initialLocation,
                structure
        );
    }

    @Override
    public void doLogin(Actor player)
    {
        MockedPlayer mockedPlayer = (MockedPlayer) player;

        Connection conn = mockedPlayer.getMockedConnection();
        activateChannel(conn);
        if (!this.dispatchLoginEvent(player, (InetSocketAddress) conn.address))
            throw new IllegalStateException("Login for " + player.getActorName() + " was denied.");

        PlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();
        playerList.placeNewPlayer(conn, mockedPlayer);
        injectServerConnection(conn);

        this.sendSettings(mockedPlayer.getBukkitEntity());
    }

    @Override
    protected Class<?> getLoginListenerClass()
    {
        return ClientLoginPacketListener.class;
    }

    @Override
    protected void injectPlayerConnection(@NotNull Player player)
    {
        // Pass
    }

    private static class MockedChannel extends EmbeddedChannel
    {
        public MockedChannel()
        {
            try
            {
                this.doRegister();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            this.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                    .addLast("splitter", new Varint21FrameDecoder())
                    .addLast("decoder", new PacketDecoder(PacketFlow.SERVERBOUND))
                    .addLast("prepender", new Varint21LengthFieldPrepender())
                    .addLast("encoder", new PacketEncoder(PacketFlow.CLIENTBOUND));
        }
    }
}
