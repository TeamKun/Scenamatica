package org.kunlab.scenamatica.nms.v1_16_R3.utils;

import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;

public class NMSSupport
{
    public static Vec3D convertLocToVec3D(Location location)
    {
        return new Vec3D(location.getX(), location.getY(), location.getZ());
    }

    public static EnumMoveType convertMoveTypeToNMS(NMSMoveType moveType)
    {
        return EnumMoveType.valueOf(moveType.name());
    }
}
