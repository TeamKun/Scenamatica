package org.kunlab.scenamatica.nms.impl.v1_15_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.NetworkManager;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.impl.v1_15_R1.player.NMSPlayerConnectionImpl;
import org.kunlab.scenamatica.nms.impl.v1_15_R1.player.NMSPlayerInteractManagerImpl;
import org.kunlab.scenamatica.nms.impl.v1_15_R1.player.NMSNetworkManagerImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.player.NMSNetworkManager;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerConnection;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

public class NMSEntityPlayerImpl extends NMSEntityHumanImpl implements NMSEntityPlayer
{
    private final Player bukkitEntity;
    private final EntityPlayer nmsEntity;

    private final NMSPlayerInteractManager interactManager;

    private NetworkManager lastNetworkManager;
    private NMSNetworkManager networkManager;
    private PlayerConnection lastConnection;
    private NMSPlayerConnection connection;

    public NMSEntityPlayerImpl(Player bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftPlayer) bukkitEntity).getHandle();

        this.interactManager = new NMSPlayerInteractManagerImpl(this.nmsEntity.playerInteractManager);
    }

    @Override
    public NMSPlayerConnection getConnection()
    {
        if (this.lastConnection != this.nmsEntity.playerConnection)
        {
            this.lastConnection = this.nmsEntity.playerConnection;
            this.connection = new NMSPlayerConnectionImpl(this.lastConnection);
        }

        return this.connection;
    }

    @Override
    public NMSNetworkManager getNetworkManager()
    {
        PlayerConnection connection = this.nmsEntity.playerConnection;
        if (connection == null)
            return null;

        if (this.lastNetworkManager != connection.networkManager)
        {
            this.lastNetworkManager = connection.networkManager;
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
        return this.nmsEntity.getProfile();
    }

    @Override
    public EntityPlayer getNMSRaw()
    {
        return this.nmsEntity;
    }
}
