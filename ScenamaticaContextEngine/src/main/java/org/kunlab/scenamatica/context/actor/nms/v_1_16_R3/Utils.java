package org.kunlab.scenamatica.context.actor.nms.v_1_16_R3;

import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.MovingObjectPosition;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class Utils
{
    public static EnumDirection getDirection(MovingObjectPosition viewer, MovingObjectPosition target)
    {
        if (viewer == null || target == null)
            return null;

        return getDirection(
                new Location(null, viewer.getPos().x, viewer.getPos().y, viewer.getPos().z),
                new Location(null, target.getPos().x, target.getPos().y, target.getPos().z)
        );
    }

    public static EnumDirection getDirection(Location viewer, Location target)
    {
        if (viewer == null || target == null)
            return null;

        double x = target.getX() - viewer.getX();
        double y = target.getY() - viewer.getY();
        double z = target.getZ() - viewer.getZ();

        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        if (absX > absY && absX > absZ)
            return x > 0 ? EnumDirection.EAST: EnumDirection.WEST;
        else if (absY > absX && absY > absZ)
            return y > 0 ? EnumDirection.UP: EnumDirection.DOWN;
        else if (absZ > absX && absZ > absY)
            return z > 0 ? EnumDirection.SOUTH: EnumDirection.NORTH;
        else
            return null;
    }

    public static EnumDirection toNMSDirection(BlockFace face)
    {
        switch (face)
        {
            default:
                /* fallthrough */
            case NORTH:
                return EnumDirection.NORTH;
            case SOUTH:
                return EnumDirection.SOUTH;
            case EAST:
                return EnumDirection.EAST;
            case WEST:
                return EnumDirection.WEST;
            case UP:
                return EnumDirection.UP;
            case DOWN:
                return EnumDirection.DOWN;
        }
    }
}
