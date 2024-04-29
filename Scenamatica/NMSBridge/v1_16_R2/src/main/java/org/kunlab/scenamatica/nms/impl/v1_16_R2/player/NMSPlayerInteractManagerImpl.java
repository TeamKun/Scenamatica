package org.kunlab.scenamatica.nms.impl.v1_16_R2.player;

import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.PlayerInteractManager;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.impl.v1_16_R2.block.NMSBlockPositionImpl;
import org.kunlab.scenamatica.nms.types.block.NMSBlockPosition;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

public class NMSPlayerInteractManagerImpl implements NMSPlayerInteractManager
{
    private final PlayerInteractManager playerInteractManager;

    public NMSPlayerInteractManagerImpl(PlayerInteractManager playerInteractManager)
    {
        this.playerInteractManager = playerInteractManager;
    }

    @Override
    public PlayerInteractManager getNMSRaw()
    {
        return this.playerInteractManager;
    }

    @Override
    public void breakBlock(@NotNull NMSBlockPosition position)
    {
        BlockPosition nmsPosition = ((NMSBlockPositionImpl) position).getNMSRaw();
        this.playerInteractManager.breakBlock(nmsPosition);
    }
}
