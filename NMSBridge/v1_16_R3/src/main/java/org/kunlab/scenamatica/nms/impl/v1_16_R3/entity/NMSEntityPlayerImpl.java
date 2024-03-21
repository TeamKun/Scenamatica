package org.kunlab.scenamatica.nms.impl.v1_16_R3.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.player.NMSPlayerConnectionImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.player.NMSPlayerInteractManagerImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerConnection;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

public class NMSEntityPlayerImpl extends NMSEntityHumanImpl implements NMSEntityPlayer
{
    private final Player bukkitEntity;
    private final EntityPlayer nmsEntity;

    private final NMSPlayerInteractManager interactManager;
    private final NMSPlayerConnection connection;

    public NMSEntityPlayerImpl(Player bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftPlayer) bukkitEntity).getHandle();

        this.interactManager = new NMSPlayerInteractManagerImpl(this.nmsEntity.playerInteractManager);
        this.connection = new NMSPlayerConnectionImpl(this.nmsEntity.playerConnection);
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
    public NMSPlayerConnection getConnection()
    {
        return this.connection;
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
