package org.kunlab.scenamatica.nms.impl.v1_13_R1.world;

import org.bukkit.entity.Entity;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.exceptions.UnsupportedNMSOperationException;
import org.kunlab.scenamatica.nms.types.world.NMSChunkProvider;
import org.kunlab.scenamatica.nms.types.world.NMSPersistentEntitySectionManager;
import org.kunlab.scenamatica.nms.types.world.NMSWorldData;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;

public class NMSWorldServerImpl implements NMSWorldServer
{
    private final World bukkitWorld;
    private final WorldServer nmsWorld;
    private final NMSWorldData worldData;
    private final NMSChunkProvider chunkProvider;

    public NMSWorldServerImpl(@NotNull World bukkitWorld)
    {
        this.bukkitWorld = bukkitWorld;
        this.nmsWorld = ((org.bukkit.craftbukkit.v1_13_R1.CraftWorld) bukkitWorld).getHandle();
        this.worldData = new NMSWorldDataImpl(this.nmsWorld.getWorldData());
        this.chunkProvider = new NMSChunkProviderImpl(this.nmsWorld.getChunkProvider());
    }

    @Override
    public WorldServer getNMSRaw()
    {
        return this.nmsWorld;
    }

    @Override
    public World getBukkit()
    {
        return this.bukkitWorld;
    }

    @Override
    public @NotNull NMSWorldData getWorldData()
    {
        return this.worldData;
    }

    @Override
    public @NotNull NMSChunkProvider getChunkProvider()
    {
        return this.chunkProvider;
    }

    @Override
    public @NotNull NMSPersistentEntitySectionManager<Entity> getEntityManager()
    {
        throw UnsupportedNMSOperationException.of(
                NMSWorldServer.class,
                "getEntityManager",
                NMSPersistentEntitySectionManager.class
        );
    }
}
