package org.kunlab.scenamatica.nms.impl.v1_16_R3.world;

import net.minecraft.server.v1_16_R3.SaveData;
import net.minecraft.server.v1_16_R3.SecondaryWorldData;
import net.minecraft.server.v1_16_R3.WorldData;
import net.minecraft.server.v1_16_R3.WorldDataServer;
import org.kunlab.scenamatica.nms.types.world.NMSWorldData;

import java.lang.reflect.Field;

public class NMSWorldDataImpl implements NMSWorldData
{
    private static final Field fA;  // Lnet/minecraft/server/v1_16_R3/SecondaryWorldData; -> a#Lnet/minecraft/server/v1_16_R3/SaveData;

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
        return this.worldData.isHardcore();
    }

    @Override
    public void setHardcore(boolean hardcore)
    {
        if (this.worldData instanceof WorldDataServer)
        {
            WorldDataServer worldDataServer = (WorldDataServer) this.worldData;
            worldDataServer.b.hardcore = hardcore;
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
            worldDataServer.b.hardcore = hardcore;
        }
    }
}
