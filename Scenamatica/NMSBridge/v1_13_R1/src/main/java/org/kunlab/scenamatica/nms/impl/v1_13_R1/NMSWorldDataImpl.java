package org.kunlab.scenamatica.nms.impl.v1_13_R1;

import net.minecraft.server.v1_13_R1.WorldData;
import org.kunlab.scenamatica.nms.types.NMSWorldData;

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
        this.worldData.g(hardcore);
    }
}
