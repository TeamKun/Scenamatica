package org.kunlab.scenamatica.context.actor.nms.v1_20_R4;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
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
    private final ClientInformation clientInfo;

    public PlayerMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        super(registry, manager, registry.getEnvironment().getActorSettings());

        this.manager = manager;
        this.clientInfo = this.createClientInformation();
    }

    private ClientInformation createClientInformation()
    {
        String locale = "ja_JP";
        int viewDistance = 0x02;
        ChatVisiblity chatMode = ChatVisiblity.FULL;
        boolean chatColors = true;
        int displayedSkinParts = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        // 0x01: cape, 0x02: jacket, 0x04: left sleeve, 0x08: right sleeve,
        // 0x10: left pants leg, 0x20: right pants leg, 0x40: hat
        HumanoidArm mainHand = HumanoidArm.RIGHT;
        boolean disableTextFiltering = true;

        return new ClientInformation(
                locale,
                viewDistance,
                chatMode,
                chatColors,
                displayedSkinParts,
                mainHand,
                disableTextFiltering,
                true
        );
    }

    @Override
    protected void sendSettings(@NotNull Player player)
    {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        ServerGamePacketListener playerConnection = entityPlayer.connection;
        playerConnection.handleClientInformation(new ServerboundClientInformationPacket(createClientInformation()));
    }

    @Override
    protected Actor createActorInstance(@NotNull World world, @NotNull PlayerStructure structure)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = createGameProfile(structure);
        ClientInformation clientInfo = createClientInformation();

        Location initialLocation = createInitialLocation(world, structure);
        return new MockedPlayer(
                this.manager, this, server, clientInfo, worldServer,
                profile, initialLocation, structure
        );
    }

    @Override
    public void doLogin(Actor player)
    {
        MockedPlayer mockedPlayer = (MockedPlayer) player;

        MockedConnection connection = mockedPlayer.getMockedConnection();
        if (!this.dispatchLoginEvent(player, (InetSocketAddress) connection.getRemoteAddress()))
            throw new IllegalStateException("Login for " + player.getActorName() + " was denied.");

        PlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();
        playerList.placeNewPlayer(
                connection,
                mockedPlayer,
                new CommonListenerCookie(((MockedPlayer) player).getGameProfile(), 0, clientInfo, false)
        );

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
}
