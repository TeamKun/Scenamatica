package org.kunlab.scenamatica.nms.impl.v1_13_R2.world;

import net.minecraft.server.v1_13_R2.ChunkProviderServer;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.exceptions.UnsupportedNMSOperationException;
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
        this.chunkProvider.unloadChunks(() -> true);
    }

    @Override
    public void removeEntity(@NotNull NMSEntity entity)
    {
        throw UnsupportedNMSOperationException.ofVoid(
                ChunkProviderServer.class,
                "removeEntity",
                NMSEntity.class
        );
    }
}
