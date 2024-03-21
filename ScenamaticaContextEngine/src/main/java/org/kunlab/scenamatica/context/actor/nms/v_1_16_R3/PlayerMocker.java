package org.kunlab.scenamatica.context.actor.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.LoginListener;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInSettings;
import net.minecraft.server.v1_16_R3.PlayerList;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.context.actor.PlayerMockerBase;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;

import java.io.IOException;
import java.net.InetSocketAddress;

public class PlayerMocker extends PlayerMockerBase
{
    private final ScenamaticaRegistry registry;
    private final ActorManager manager;

    public PlayerMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        super(registry, registry.getEnvironment().getActorSettings());

        this.registry = registry;
        this.manager = manager;
    }

    @SneakyThrows(IOException.class)
    private static void sendSettings(EntityPlayer player)
    {
        String locale = player.getBukkitEntity().getLocale();
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

        player.a(packet);
    }

    private void registerPlayer(MinecraftServer server, MockedPlayer player)
    {
        PlayerList list = server.getPlayerList();
        list.a(player.getNetworkManager(), player);
        sendSettings(player);
    }

    @Override
    @NotNull
    public Actor mock(@NotNull World world, @NotNull PlayerStructure structure)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        NetworkManager mockedNetworkManager = new MockedNetworkManager(server, structure);
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = createGameProfile(structure);
        boolean doLogin = structure.getOnline() == null || structure.getOnline();

        Location initialLocation;
        if (structure.getLocation() != null)
            initialLocation = structure.getLocation().create().clone();
        else
            initialLocation = world.getSpawnLocation().clone();

        if (structure.getLocation() != null && structure.getLocation().getWorld() == null)
            initialLocation.setWorld(world);

        MockedPlayer player = new MockedPlayer(
                this.manager, this, mockedNetworkManager, server, worldServer,
                profile, initialLocation, structure
        );
        super.initActor(player.getBukkitEntity(), structure);

        if (doLogin)
            this.doLogin(server, player);

        return player;
    }

    /* non-public */ void doLogin(MinecraftServer server, MockedPlayer player)
    {
        if (!this.dispatchLoginEvent(player, (InetSocketAddress) player.getNetworkManager().getSocketAddress()))
            throw new IllegalStateException("Login for " + player.getName() + " was denied.");

        this.registerPlayer(server, player);
    }

    @Override
    protected Class<?> getLoginListenerClass()
    {
        return LoginListener.class;
    }

    @Override
    public void postActorLogin(Player player)
    {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        assert entityPlayer instanceof MockedPlayer;
        MockedPlayer mockedPlayer = (MockedPlayer) entityPlayer;

        mockedPlayer.playerConnection = new MockedPlayerConnection(
                mockedPlayer.server,
                mockedPlayer.playerConnection.networkManager,
                mockedPlayer
        );

        super.moveToLocationSafe(player, mockedPlayer.getInitialLocation());
    }

}
