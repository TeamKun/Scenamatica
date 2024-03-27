package org.kunlab.scenamatica.commons.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class VoxelUtils
{
    public static Location getFrontOf(Location viewer, Location target, double distance)
    {
        Vector direction = target.toVector().subtract(viewer.toVector()).normalize();

        return target.clone()
                .add(direction.multiply(distance));
    }

    public static Location getFrontOf(Location viewer, BlockFace face, double distance)
    {
        Vector direction = face.getDirection();

        return viewer.clone().add(direction.multiply(distance));
    }

    public static BlockFace getOpenFace(Location target)
    {
        for (BlockFace face : BlockFace.values())
        {
            if (target.getBlock().getRelative(face).isEmpty())
                return face;
        }

        throw new IllegalStateException("No open face found");
    }

    public static BlockFace traceDirection(Location viewer, Location target)
    {
        double x = target.getX() - viewer.getX();
        double y = target.getY() - viewer.getY();
        double z = target.getZ() - viewer.getZ();

        double xz = Math.sqrt(x * x + z * z);

        double pitch = Math.atan2(y, xz);
        double yaw = Math.atan2(z, x);

        return traceDirection(pitch, yaw);
    }

    public static BlockFace traceDirection(double pitch, double yaw)
    {
        yaw = Math.toDegrees(yaw);

        // [-180, 180] に正規化
        yaw = (yaw + 180.0) % 360.0 - 180.0;

        if (pitch < -45)
            return BlockFace.UP;
        else if (pitch > 45)
            return BlockFace.DOWN;
        else
        {
            if (yaw < -135)
                return BlockFace.NORTH;
            else if (yaw < -45)
                return BlockFace.EAST;
            else if (yaw < 45)
                return BlockFace.SOUTH;
            else if (yaw < 135)
                return BlockFace.WEST;
            else
                return BlockFace.NORTH;
        }

    }
}
