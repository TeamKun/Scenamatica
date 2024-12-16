package org.kunlab.scenamatica.nms.impl.v1_18_R1.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
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
        this.worldData = new NMSWorldDataImpl(this.nmsWorld.N);
        this.chunkProvider = new NMSChunkProviderImpl(this.nmsWorld.getChunkSource());
        this.entityManager = new NMSPersistentEntitySectionManagerImpl<>(this.nmsWorld.entityManager);
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

    @Override
    public void playBlockAction(Block block, int param1, int param2)
    {
        BlockPos blockPosition = new BlockPos(block.getX(), block.getY(), block.getZ());
        net.minecraft.world.level.block.Block nmsBlock = CraftMagicNumbers.getBlock(block.getBlockData().getMaterial());
        this.nmsWorld.blockEvent(blockPosition, nmsBlock, param1, param2);
    }
}
