package org.kunlab.scenamatica.nms.impl.v1_18_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.impl.v1_18_R1.player.NMSNetworkManagerImpl;
import org.kunlab.scenamatica.nms.impl.v1_18_R1.player.NMSPlayerConnectionImpl;
import org.kunlab.scenamatica.nms.impl.v1_18_R1.player.NMSPlayerInteractManagerImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.player.NMSNetworkManager;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerConnection;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

public class NMSEntityPlayerImpl extends NMSEntityHumanImpl implements NMSEntityPlayer
{
    private final Player bukkitEntity;
    private final ServerPlayer nmsEntity;
    private final NMSPlayerInteractManager interactManager;
    private Connection lastNetworkManager;
    private NMSNetworkManager networkManager;
    private ServerGamePacketListenerImpl lastConnection;
    private NMSPlayerConnection connection;

    public NMSEntityPlayerImpl(Player bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftPlayer) bukkitEntity).getHandle();

        this.interactManager = new NMSPlayerInteractManagerImpl(this.nmsEntity.gameMode);
    }

    @Override
    public NMSPlayerConnection getConnection()
    {
        if (this.lastConnection != this.nmsEntity.connection)
        {
            this.lastConnection = this.nmsEntity.connection;
            this.connection = new NMSPlayerConnectionImpl(this.lastConnection);
        }

        return this.connection;
    }

    @Override
    public NMSNetworkManager getNetworkManager()
    {
        ServerGamePacketListenerImpl connection = this.nmsEntity.connection;
        if (connection == null)
            return null;

        if (this.lastNetworkManager != connection.connection)
        {
            this.lastNetworkManager = connection.connection;
            this.networkManager = new NMSNetworkManagerImpl(this.lastNetworkManager);
        }

        return this.networkManager;
    }

    @Override
    public Player getBukkit()
    {
        return this.bukkitEntity;
    }

    @Override
    public NMSPlayerInteractManager getInteractManager()
    {
        return this.interactManager;
    }

    @Override
    public GameProfile getProfile()
    {
        return this.nmsEntity.getBukkitEntity().getProfile();
    }

    @Override
    public ServerPlayer getNMSRaw()
    {
        return this.nmsEntity;
    }
}
