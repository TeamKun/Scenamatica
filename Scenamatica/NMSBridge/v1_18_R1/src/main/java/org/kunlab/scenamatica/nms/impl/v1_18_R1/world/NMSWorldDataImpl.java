package org.kunlab.scenamatica.nms.impl.v1_18_R1.world;

import net.minecraft.world.level.storage.SaveData;
import net.minecraft.world.level.storage.SecondaryWorldData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.WorldDataServer;
import org.kunlab.scenamatica.nms.types.world.NMSWorldData;

import java.lang.reflect.Field;

public class NMSWorldDataImpl implements NMSWorldData
{
    private static final Field fA;  // Lnet/minecraft/server/v1_18_R1/SecondaryWorldData; -> a#Lnet/minecraft/server/SaveData;

    static
    {
        try
        {
            fA = SecondaryWorldData.class.getDeclaredField("a");
            fA.setAccessible(true);
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
        return this.worldData.n();
    }

    @Override
    public void setHardcore(boolean hardcore)
    {
        if (this.worldData instanceof WorldDataServer)
        {
            WorldDataServer worldDataServer = (WorldDataServer) this.worldData;
            worldDataServer.e.c = hardcore;
        }
        else /* assert this.worldData instanceof SecondaryWorldData */
        {
            SecondaryWorldData secondaryWorldData = (SecondaryWorldData) this.worldData;
            SaveData saveData;
            try
            {
                saveData = (SaveData) fA.get(secondaryWorldData);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }

            assert saveData instanceof WorldDataServer;
            WorldDataServer worldDataServer = (WorldDataServer) saveData;
            worldDataServer.e.c = hardcore;
        }
    }
}
