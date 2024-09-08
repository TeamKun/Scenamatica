package org.kunlab.scenamatica.nms.impl.v1_13_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.PlayerList;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.types.NMSPlayerList;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
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
    @NotNull
    public NMSEntityPlayer moveToWorld(@NotNull NMSEntityPlayer player,
                                       @NotNull NMSWorldServer world,
                                       boolean shouldCopyState,
                                       @Nullable Location locationToSpawn,
                                       boolean avoidSuffocation)
    {
        EntityPlayer nmsPlayer = (EntityPlayer) player.getNMSRaw();
        WorldServer nmsWorld = (WorldServer) world.getNMSRaw();

        this.nmsPlayerList.moveToWorld(
                nmsPlayer,
                nmsWorld.dimension,
                shouldCopyState,
                locationToSpawn,
                avoidSuffocation
        );

        return player;
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
