package org.kunlab.scenamatica.nms.impl.v1_15_R1;

import net.minecraft.server.v1_15_R1.SecondaryWorldData;
import net.minecraft.server.v1_15_R1.WorldData;
import org.kunlab.scenamatica.nms.types.NMSWorldData;

import java.lang.reflect.Field;

public class NMSWorldDataImpl implements NMSWorldData
{
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
        this.worldData.setHardcore(true);
    }
}
