package org.kunlab.scenamatica.nms.impl.v1_20_R3.utils;

import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

public class NMSSupport
{
    public static Vec3 convertLocToVec3D(Location location)
    {
        return new Vec3(location.getX(), location.getY(), location.getZ());
    }
}
