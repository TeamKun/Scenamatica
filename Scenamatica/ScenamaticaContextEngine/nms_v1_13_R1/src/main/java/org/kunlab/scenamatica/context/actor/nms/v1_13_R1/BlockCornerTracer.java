package org.kunlab.scenamatica.context.actor.nms.v1_13_R1;

import lombok.Value;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.FluidCollisionOption;
import net.minecraft.server.v1_13_R1.MovingObjectPosition;
import net.minecraft.server.v1_13_R1.Vec3D;
import org.bukkit.Location;

public class BlockCornerTracer
{
    public static TraceResult trace(EntityHuman entity, Location block)
    {
        float pitch = entity.pitch;
        float yaw = entity.yaw;

        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        // 交点を求める
        Vec3D start = new Vec3D(entity.locX, entity.locY + entity.getHeadHeight(), entity.locZ);
        Vec3D end = new Vec3D(block.getX(), block.getY(), block.getZ());

        MovingObjectPosition result = entity.world.rayTrace(start, end, FluidCollisionOption.NEVER);
        if (result == null)
            return new TraceResult(0, 0, 0);

        return new TraceResult((float) result.pos.x, (float) result.pos.y, (float) result.pos.z);
    }

    @Value
    public static class TraceResult
    {
        float x;
        float y;
        float z;
    }
}
