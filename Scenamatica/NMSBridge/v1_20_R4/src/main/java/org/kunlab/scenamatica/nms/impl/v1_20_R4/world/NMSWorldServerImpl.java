package org.kunlab.scenamatica.nms.impl.v1_20_R4.world;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.types.world.NMSChunkProvider;
import org.kunlab.scenamatica.nms.types.world.NMSPersistentEntitySectionManager;
import org.kunlab.scenamatica.nms.types.world.NMSWorldData;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;

public class NMSWorldServerImpl implements NMSWorldServer
{
    private final World bukkitWorld;
    private final ServerLevel nmsWorld;
    private final NMSWorldData worldData;
    private final NMSChunkProvider chunkProvider;
    private final NMSPersistentEntitySectionManager<Entity> entityManager;

    public NMSWorldServerImpl(@NotNull World bukkitWorld)
    {
        this.bukkitWorld = bukkitWorld;
        this.nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        this.worldData = new NMSWorldDataImpl(this.nmsWorld.K);
        this.chunkProvider = new NMSChunkProviderImpl(this.nmsWorld.getChunkSource());
        this.entityManager = new NMSPersistentEntitySectionManagerImpl<>(this.nmsWorld.P);
    }

    @Override
    public ServerLevel getNMSRaw()
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
        return this.entityManager;
    }
}
