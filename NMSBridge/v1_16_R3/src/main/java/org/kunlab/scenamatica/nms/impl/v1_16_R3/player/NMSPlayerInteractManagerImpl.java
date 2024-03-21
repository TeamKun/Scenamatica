package org.kunlab.scenamatica.nms.impl.v1_16_R3.player;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.TypeSupportImpl;
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
    public Object getNMSRaw()
    {
        return this.playerInteractManager;
    }

    @Override
    public Object getBukkit()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void breakBlock(@NotNull NMSBlockPosition position)
    {
        BlockPosition nmsPosition = TypeSupportImpl.toNMS(position);
        this.playerInteractManager.breakBlock(nmsPosition);
    }
}
