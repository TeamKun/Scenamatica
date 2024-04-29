package org.kunlab.scenamatica.nms.impl.v1_16_R2;

import net.minecraft.server.v1_16_R2.SaveData;
import net.minecraft.server.v1_16_R2.SecondaryWorldData;
import net.minecraft.server.v1_16_R2.WorldData;
import net.minecraft.server.v1_16_R2.WorldDataServer;
import org.kunlab.scenamatica.nms.types.NMSWorldData;

import java.lang.reflect.Field;

public class NMSWorldDataImpl implements NMSWorldData
{
    private static final Field fA;  // Lnet/minecraft/server/v1_16_R2/SecondaryWorldData; -> a#Lnet/minecraft/server/v1_16_R2/SaveData;

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
