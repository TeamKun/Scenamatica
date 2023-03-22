package net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.scenamatica.context.player.PlayerMockerBase;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.LoginListener;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInSettings;
import net.minecraft.server.v1_16_R3.PlayerList;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.entity.Player;

import java.io.IOException;

public class PlayerMocker extends PlayerMockerBase
{
    public PlayerMocker(ScenamaticaRegistry registry)
    {
        super(registry);
    }

    private void registerPlayer(MinecraftServer server, MockedPlayer player)
    {
        PlayerList list = server.getPlayerList();
        NetworkManager mockedNetworkManager = new MockedNetworkManager(this, player, server);
        list.a(mockedNetworkManager, player);
        sendSettings(player);

        Runner.runLater(() -> player.playerConnection = new MockedPlayerConnection(server, mockedNetworkManager, player), 20);
    }

    @SneakyThrows(IOException.class)
    private static void sendSettings(EntityPlayer player)
    {
        String locale = "en_US";
        int viewDistance = 0x02;
        int chatMode = 0;  // 0: enabled, 1: commands only, 2: hidden
        boolean chatColors = true;
        int displayedSkinParts = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        // 0x01: cape, 0x02: jacket, 0x04: left sleeve, 0x08: right sleeve,
        // 0x10: left pants leg, 0x20: right pants leg, 0x40: hat
        int mainHand = 1; // 0: left, 1: right

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

    @Override
    public Player mock(PlayerBean bean)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.E(); // Get overworld(world) server.
        GameProfile profile = createGameProfile(bean);

        MockedPlayer player = new MockedPlayer(server, worldServer, profile);
        if (!dispatchLoginEvent(player.getBukkitEntity()))
            return null;

        this.registerPlayer(server, player);

        return player.getBukkitEntity();
    }

    @Override
    public void unmock(Player player)
    {
        if (!(player instanceof MockedPlayer))
            return;

        MockedPlayer mockedPlayer = (MockedPlayer) player;

        MinecraftServer server = mockedPlayer.getMinecraftServer();
        assert server != null;
        PlayerList list = server.getPlayerList();

        if (!mockedPlayer.playerConnection.isDisconnected())
            list.disconnect(mockedPlayer);

        this.wipePlayerData(mockedPlayer.getUniqueID());
    }

    @Override
    protected Class<?> getLoginListenerClass()
    {
        return LoginListener.class;
    }

}
