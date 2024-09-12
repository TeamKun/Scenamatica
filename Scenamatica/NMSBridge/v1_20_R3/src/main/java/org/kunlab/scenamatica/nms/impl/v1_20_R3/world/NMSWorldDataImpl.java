package org.kunlab.scenamatica.nms.impl.v1_20_R3.world;

import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.kunlab.scenamatica.nms.types.world.NMSWorldData;

import java.lang.reflect.Field;

public class NMSWorldDataImpl implements NMSWorldData
{
    private static final Field fWorldData;  // Lnet/minecraft/world/level/storage/DerivedLevelData; -> worldData#Lnet/minecraft/world/leve/storage/WorldData

    static
    {
        try
        {
            fWorldData = DerivedLevelData.class.getDeclaredField("worldData");
            fWorldData.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private final WorldData worldData;

    public NMSWorldDataImpl(WorldData worldData)
    {
        this.worldData = worldData;
    }

    @Override
    public boolean isHardcore()
    {
        return this.worldData.isHardcore();
    }

    @Override
    public void setHardcore(boolean hardcore)
    {
        if (this.worldData instanceof PrimaryLevelData)
        {
            PrimaryLevelData worldDataServer = (PrimaryLevelData) this.worldData;
            worldDataServer.settings.hardcore = hardcore;
        }
        else /* assert this.worldData instanceof DerivedLevelData */
        {
            DerivedLevelData secondaryWorldData = (DerivedLevelData) this.worldData;
            WorldData data;
            try
            {
                data = (WorldData) fWorldData.get(secondaryWorldData);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }

            PrimaryLevelData worldDataServer = (PrimaryLevelData) data;
            worldDataServer.settings.hardcore = true;
        }
    }
}
