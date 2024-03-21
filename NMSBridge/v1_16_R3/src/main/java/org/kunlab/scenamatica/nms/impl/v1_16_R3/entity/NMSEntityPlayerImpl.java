package org.kunlab.scenamatica.nms.impl.v1_16_R3.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.player.NMSNetworkManagerImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.player.NMSPlayerConnectionImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.player.NMSPlayerInteractManagerImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.player.NMSNetworkManager;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerConnection;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

public class NMSEntityPlayerImpl extends NMSEntityHumanImpl implements NMSEntityPlayer
{
    private final Player bukkitEntity;
    private final EntityPlayer nmsEntity;

    private final NMSPlayerInteractManager interactManager;
    private final NMSNetworkManager networkManager;

    private PlayerConnection lastConnection;
    private NMSPlayerConnection connection;

    public NMSEntityPlayerImpl(Player bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftPlayer) bukkitEntity).getHandle();

        this.networkManager = new NMSNetworkManagerImpl(this.nmsEntity.playerConnection.networkManager);
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
