package org.kunlab.scenamatica.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.World;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;

public class NMSWorldServerImpl implements NMSWorldServer
{
    private final World bukkitWorld;
    private final WorldServer nmsWorld;

    public NMSWorldServerImpl(World bukkitWorld)
    {
        this.bukkitWorld = bukkitWorld;
        this.nmsWorld = ((org.bukkit.craftbukkit.v1_16_R3.CraftWorld) bukkitWorld).getHandle();
    }

    @Override
    public Object getNMSRaw()
    {
        return this.nmsWorld;
    }

    @Override
    public Object getNMSCraftRaw()
    {
        return this.bukkitWorld;
    }
}
