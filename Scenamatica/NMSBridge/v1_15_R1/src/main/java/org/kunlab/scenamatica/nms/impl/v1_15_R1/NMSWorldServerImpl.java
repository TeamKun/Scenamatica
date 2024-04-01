package org.kunlab.scenamatica.nms.impl.v1_15_R1;

import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.types.NMSWorldData;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;

public class NMSWorldServerImpl implements NMSWorldServer
{
    private final World bukkitWorld;
    private final WorldServer nmsWorld;
    private final NMSWorldData worldData;

    public NMSWorldServerImpl(World bukkitWorld)
    {
        this.bukkitWorld = bukkitWorld;
        this.nmsWorld = ((org.bukkit.craftbukkit.v1_15_R1.CraftWorld) bukkitWorld).getHandle();
        this.worldData = new NMSWorldDataImpl(this.nmsWorld.getWorldData());
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
}
