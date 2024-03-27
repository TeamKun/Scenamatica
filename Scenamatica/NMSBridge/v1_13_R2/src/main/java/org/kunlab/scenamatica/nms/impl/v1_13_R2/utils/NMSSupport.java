package org.kunlab.scenamatica.nms.impl.v1_13_R2.utils;

import net.minecraft.server.v1_13_R2.Vec3D;
import org.bukkit.Location;

public class NMSSupport
{
    public static Vec3D convertLocToVec3D(Location location)
    {
        return new Vec3D(location.getX(), location.getY(), location.getZ());
    }
}
