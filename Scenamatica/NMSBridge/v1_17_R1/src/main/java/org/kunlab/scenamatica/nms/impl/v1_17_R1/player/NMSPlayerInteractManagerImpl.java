package org.kunlab.scenamatica.nms.impl.v1_17_R1.player;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.PlayerInteractManager;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.block.NMSBlockPositionImpl;
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
