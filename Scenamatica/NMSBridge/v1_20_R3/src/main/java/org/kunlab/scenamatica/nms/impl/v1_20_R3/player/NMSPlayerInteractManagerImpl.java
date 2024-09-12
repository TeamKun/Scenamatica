package org.kunlab.scenamatica.nms.impl.v1_20_R3.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.impl.v1_20_R3.block.NMSBlockPositionImpl;
import org.kunlab.scenamatica.nms.types.block.NMSBlockPosition;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerInteractManager;

public class NMSPlayerInteractManagerImpl implements NMSPlayerInteractManager
{
    private final ServerPlayerGameMode playerInteractManager;

    public NMSPlayerInteractManagerImpl(ServerPlayerGameMode playerInteractManager)
    {
        this.playerInteractManager = playerInteractManager;
    }

    @Override
    public ServerPlayerGameMode getNMSRaw()
    {
        return this.playerInteractManager;
    }

    @Override
    public void breakBlock(@NotNull NMSBlockPosition position)
    {
        BlockPos nmsPosition = ((NMSBlockPositionImpl) position).getNMSRaw();
        this.playerInteractManager.destroyBlock(nmsPosition);
    }
}
