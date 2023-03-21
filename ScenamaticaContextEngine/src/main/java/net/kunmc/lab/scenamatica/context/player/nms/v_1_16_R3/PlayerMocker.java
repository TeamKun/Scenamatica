package net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import net.kunmc.lab.scenamatica.context.player.PlayerMockerBase;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PlayerList;
import net.minecraft.server.v1_16_R3.WorldNBTStorage;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerMocker extends PlayerMockerBase
{
    private final ScenamaticaRegistry registry;

    public PlayerMocker(ScenamaticaRegistry registry)
    {
        this.registry = registry;
    }

    private static void registerPlayer(MinecraftServer server, EntityPlayer player)
    {
        PlayerList list = server.getPlayerList();
        NetworkManager mockedNetworkManager = new MockedNetworkManager();
        list.a(mockedNetworkManager, player);
        player.playerConnection = new MockedPlayerConnection(server, mockedNetworkManager, player);
    }

    @Override
    public Player mock(PlayerBean bean)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.E(); // Get overworld(world) server.
        GameProfile profile = createGameProfile(bean);

        MockedPlayer player = new MockedPlayer(server, worldServer, profile);
        registerPlayer(server, player);

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

        list.disconnect(mockedPlayer);
        this.wipePlayerData(list, mockedPlayer.getUniqueID());
    }

    private void wipePlayerData(PlayerList list, UUID uuid)
    {
        WorldNBTStorage storage = list.playerFileData;
        Path baseDir = storage.getPlayerDir().toPath();

        Path playerData = baseDir.resolve(uuid.toString() + ".dat");
        Path playerDataOld = baseDir.resolve(uuid + ".dat_old");

        try
        {
            Files.deleteIfExists(playerData);
            Files.deleteIfExists(playerDataOld);
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
        }
    }
}
