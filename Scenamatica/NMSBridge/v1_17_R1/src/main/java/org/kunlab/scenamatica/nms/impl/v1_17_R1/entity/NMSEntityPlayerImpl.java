package org.kunlab.scenamatica.nms.impl.v1_17_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.player.NMSNetworkManagerImpl;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.player.NMSPlayerConnectionImpl;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.player.NMSPlayerInteractManagerImpl;
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

        this.interactManager = new NMSPlayerInteractManagerImpl(this.nmsEntity.d);
    }

    @Override
    public NMSPlayerConnection getConnection()
    {
        if (this.lastConnection != this.nmsEntity.b)
        {
            this.lastConnection = this.nmsEntity.b;
            this.connection = new NMSPlayerConnectionImpl(this.lastConnection);
        }

        return this.connection;
    }

    @Override
    public NMSNetworkManager getNetworkManager()
    {
        PlayerConnection connection = this.nmsEntity.b;
        if (connection == null)
            return null;

        if (this.lastNetworkManager != connection.a)
        {
            this.lastNetworkManager = connection.a;
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
