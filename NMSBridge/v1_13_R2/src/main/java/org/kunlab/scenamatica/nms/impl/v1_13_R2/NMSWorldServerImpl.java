package org.kunlab.scenamatica.nms.impl.v1_13_R2;

import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.World;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;

public class NMSWorldServerImpl implements NMSWorldServer
{
    private final World bukkitWorld;
    private final WorldServer nmsWorld;

    public NMSWorldServerImpl(World bukkitWorld)
    {
        this.bukkitWorld = bukkitWorld;
        this.nmsWorld = ((org.bukkit.craftbukkit.v1_13_R2.CraftWorld) bukkitWorld).getHandle();
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
}
