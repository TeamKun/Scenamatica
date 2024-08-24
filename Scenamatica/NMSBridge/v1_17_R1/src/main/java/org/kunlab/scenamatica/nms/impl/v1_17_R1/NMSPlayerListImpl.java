package org.kunlab.scenamatica.nms.impl.v1_17_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.types.NMSPlayerList;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.player.NMSNetworkManager;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;

public class NMSPlayerListImpl implements NMSPlayerList
{
    private final PlayerList nmsPlayerList;

    public NMSPlayerListImpl(PlayerList nmsPlayerList)
    {
        this.nmsPlayerList = nmsPlayerList;
    }

    @Override
    public PlayerList getNMSRaw()
    {
        return this.nmsPlayerList;
    }

    @Override
    public Server getBukkit()
    {
        return Bukkit.getServer();
    }

    @Override
    public @NotNull NMSEntityPlayer moveToWorld(@NotNull NMSEntityPlayer player,
                                                @NotNull NMSWorldServer world,
                                                boolean shouldCopyState,
                                                @Nullable Location locationToSpawn,
                                                boolean avoidSuffocation)
    {
        EntityPlayer nmsPlayer = (EntityPlayer) player.getNMSRaw();
        WorldServer nmsWorld = (WorldServer) world.getNMSRaw();

        this.nmsPlayerList.moveToWorld(
                nmsPlayer,
                nmsWorld,
                shouldCopyState,
                locationToSpawn,
                avoidSuffocation
        );

        return player;
    }

    @Override
    public void registerPlayer(NMSNetworkManager networkManager, NMSEntityPlayer player)
    {
        NetworkManager nmsNetworkManager = (NetworkManager) networkManager.getNMSRaw();
        EntityPlayer nmsPlayer = (EntityPlayer) player.getNMSRaw();

        this.nmsPlayerList.a(nmsNetworkManager, nmsPlayer);
    }

    @Override
    public boolean isOp(@NotNull GameProfile profile)
    {
        return this.nmsPlayerList.isOp(profile);
    }

    @Override
    public void addOp(@NotNull GameProfile profile)
    {
        this.nmsPlayerList.addOp(profile);
    }

    @Override
    public void removeOp(@NotNull GameProfile profile)
    {
        this.nmsPlayerList.removeOp(profile);
    }
}
