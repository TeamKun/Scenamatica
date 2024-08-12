package org.kunlab.scenamatica.nms.impl.v1_14_R1.world;

import net.minecraft.server.v1_14_R1.WorldData;
import org.kunlab.scenamatica.nms.types.world.NMSWorldData;

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
