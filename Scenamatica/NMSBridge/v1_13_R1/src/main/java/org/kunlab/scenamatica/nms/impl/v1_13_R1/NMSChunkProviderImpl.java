package org.kunlab.scenamatica.nms.impl.v1_13_R1;

import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.ChunkProviderServer;
import net.minecraft.server.v1_13_R1.IChunkProvider;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.exceptions.UnsupportedNMSOperationException;
import org.kunlab.scenamatica.nms.types.NMSChunkProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;

import java.util.Iterator;

public class NMSChunkProviderImpl implements NMSChunkProvider
{
    private final ChunkProviderServer chunkProvider;

    public NMSChunkProviderImpl(IChunkProvider chunkProvider)
    {
        this.chunkProvider = (ChunkProviderServer) chunkProvider;
    }

    @Override
    public void purgeUnload()
    {
        this.chunkProvider.unloadChunks();
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
