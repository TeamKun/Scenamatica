package org.kunlab.scenamatica.context.actor.nms.v1_16_R1;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.LoginListener;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import net.minecraft.server.v1_16_R1.NetworkManager;
import net.minecraft.server.v1_16_R1.PacketDataSerializer;
import net.minecraft.server.v1_16_R1.PacketPlayInSettings;
import net.minecraft.server.v1_16_R1.PlayerList;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
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
    private final ScenamaticaRegistry registry;
    private final ActorManager manager;

    public PlayerMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        super(registry, manager, registry.getEnvironment().getActorSettings());

        this.registry = registry;
        this.manager = manager;
    }

    @Override
    @SneakyThrows(IOException.class)
    protected void sendSettings(@NotNull Player player)
    {
        String locale = player.getLocale();
        int viewDistance = 0x02;
        int chatMode = 0;  // 0: enabled, 1: commands only, 2: hidden
        boolean chatColors = true;
        int displayedSkinParts = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        // 0x01: cape, 0x02: jacket, 0x04: left sleeve, 0x08: right sleeve,
        // 0x10: left pants leg, 0x20: right pants leg, 0x40: hat
        int mainHand = player.getMainHand().ordinal();

        PacketDataSerializer serializer = new PacketDataSerializer(ByteBufAllocator.DEFAULT.buffer());
        serializer.a(locale);
        serializer.d(viewDistance);
        serializer.d(chatMode);
        serializer.writeBoolean(chatColors);
        serializer.d(displayedSkinParts);
        serializer.d(mainHand);

        PacketPlayInSettings packet = new PacketPlayInSettings();
        packet.a(serializer);

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
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

        mockedPlayer.playerConnection = new MockedPlayerConnection(
                this.registry,
                mockedPlayer.server,
                mockedPlayer.playerConnection.networkManager,
                mockedPlayer
        );
    }
}
