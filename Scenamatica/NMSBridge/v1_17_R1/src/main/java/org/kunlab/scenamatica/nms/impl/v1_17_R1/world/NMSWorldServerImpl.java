package org.kunlab.scenamatica.nms.impl.v1_17_R1.world;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.entity.EntityTypes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSLightningStrikeCause;
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
    private final NMSPersistentEntitySectionManager<Entity> entityManager;

    public NMSWorldServerImpl(@NotNull World bukkitWorld)
    {
        this.bukkitWorld = bukkitWorld;
        this.nmsWorld = ((org.bukkit.craftbukkit.v1_17_R1.CraftWorld) bukkitWorld).getHandle();
        this.worldData = new NMSWorldDataImpl(this.nmsWorld.getWorldData());
        this.chunkProvider = new NMSChunkProviderImpl(this.nmsWorld.getChunkProvider());
        this.entityManager = new NMSPersistentEntitySectionManagerImpl<>(this.nmsWorld.G);
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
        return this.entityManager;
    }

    @Override
    public void strikeLightning(@NotNull Location location, boolean isEffect, @NotNull NMSLightningStrikeCause cause)
    {
        EntityLightning nmsEntity = new EntityLightning(EntityTypes.U, this.nmsWorld);
        nmsEntity.ap = isEffect;
        nmsEntity.setPosition(location.getX(), location.getY(), location.getZ());
        this.nmsWorld.strikeLightning(nmsEntity);
    }

    @Override
    public void strikeLightning(@NotNull LightningStrike strike, @NotNull NMSLightningStrikeCause cause)
    {
        EntityLightning lightning = ((org.bukkit.craftbukkit.v1_17_R1.entity.CraftLightningStrike) strike).getHandle();
        this.nmsWorld.strikeLightning(lightning, NMSLightningStrikeCause.toBukkit(cause));
    }

    @Override
    public void playBlockAction(Block block, int param1, int param2)
    {
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        net.minecraft.world.level.block.Block nmsBlock = CraftMagicNumbers.getBlock(block.getBlockData().getMaterial());
        this.nmsWorld.playBlockAction(blockPosition, nmsBlock, param1, param2);
    }
}
