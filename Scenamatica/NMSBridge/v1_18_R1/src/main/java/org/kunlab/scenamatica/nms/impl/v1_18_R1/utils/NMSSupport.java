package org.kunlab.scenamatica.nms.impl.v1_18_R1.utils;

import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

public class NMSSupport
{
    public static Vec3 convertLocToVec3D(Location location)
    {
        return new Vec3(location.getX(), location.getY(), location.getZ());
    }
}
