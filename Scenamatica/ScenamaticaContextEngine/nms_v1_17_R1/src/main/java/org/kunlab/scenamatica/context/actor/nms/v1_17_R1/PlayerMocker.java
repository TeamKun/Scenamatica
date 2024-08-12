package org.kunlab.scenamatica.context.actor.nms.v1_17_R1;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayInSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.LoginListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.player.EnumChatVisibility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.context.actor.PlayerMockerBase;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

import java.io.IOException;
import java.net.InetSocketAddress;

public class PlayerMocker extends PlayerMockerBase
{
    private final ActorManager manager;

    public PlayerMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        super(registry, manager, registry.getEnvironment().getActorSettings());

        this.manager = manager;
    }

    @Override
    protected void sendSettings(@NotNull Player player)
    {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        String locale = player.getLocale();
        int viewDistance = 0x02;
        EnumChatVisibility chatMode = EnumChatVisibility.a;
        boolean chatColors = true;
        int displayedSkinParts = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        // 0x01: cape, 0x02: jacket, 0x04: left sleeve, 0x08: right sleeve,
        // 0x10: left pants leg, 0x20: right pants leg, 0x40: hat
        EnumMainHand mainHand = entityPlayer.getMainHand();
        boolean disableTextFiltering = true;

        PacketPlayInSettings packet = new PacketPlayInSettings(
                locale,
                viewDistance,
                chatMode,
                chatColors,
                displayedSkinParts,
                mainHand,
                disableTextFiltering
        );
        entityPlayer.a(packet);
    }

    @Override
    protected Actor createActorInstance(@NotNull World world, @NotNull PlayerStructure structure)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        NetworkManager mockedNetworkManager = new MockedNetworkManager(server, structure);
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = createGameProfile(structure);

        Location initialLocation = createInitialLocation(world, structure);
        return new MockedPlayer(
                this.manager, this, mockedNetworkManager, server, worldServer,
                profile, initialLocation, structure
        );
    }

    @Override
    public void doLogin(Actor player)
    {
        MockedPlayer mockedPlayer = (MockedPlayer) player;

        NetworkManager networkManager = mockedPlayer.getNetworkManager();
        if (!this.dispatchLoginEvent(player, (InetSocketAddress) networkManager.getSocketAddress()))
            throw new IllegalStateException("Login for " + player.getName() + " was denied.");

        PlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();
        playerList.a(networkManager, mockedPlayer);

        this.sendSettings(mockedPlayer.getBukkitEntity());
    }

    @Override
    protected Class<?> getLoginListenerClass()
    {
        return LoginListener.class;
    }

    @Override
    protected void injectPlayerConnection(@NotNull Player player)
    {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        assert entityPlayer instanceof MockedPlayer;
        MockedPlayer mockedPlayer = (MockedPlayer) entityPlayer;

        mockedPlayer.b = new MockedPlayerConnection(
                mockedPlayer.c,
                mockedPlayer.b.a,
                mockedPlayer
        );
    }
}
