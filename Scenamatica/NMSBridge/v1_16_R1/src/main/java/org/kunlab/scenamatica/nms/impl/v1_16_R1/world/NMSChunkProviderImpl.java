package org.kunlab.scenamatica.nms.impl.v1_16_R1.world;

import net.minecraft.server.v1_16_R1.ChunkProviderServer;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.impl.v1_16_R1.entity.NMSEntityImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.world.NMSChunkProvider;

public class NMSChunkProviderImpl implements NMSChunkProvider
{
    private final ChunkProviderServer chunkProvider;

    public NMSChunkProviderImpl(ChunkProviderServer chunkProvider)
    {
        this.chunkProvider = chunkProvider;
    }

    @Override
    public void purgeUnload()
    {
        this.chunkProvider.purgeUnload();
    }

    @Override
    public void removeEntity(@NotNull NMSEntity entity)
    {
        this.chunkProvider.removeEntity(((NMSEntityImpl) entity).getNMSRaw());
    }
}
