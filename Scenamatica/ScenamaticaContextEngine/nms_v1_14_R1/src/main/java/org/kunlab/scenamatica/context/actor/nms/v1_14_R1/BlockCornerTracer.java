package org.kunlab.scenamatica.context.actor.nms.v1_14_R1;

import lombok.Value;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.RayTrace;
import net.minecraft.server.v1_14_R1.Vec3D;
import org.bukkit.Location;

public class BlockCornerTracer
{
    public static TraceResult trace(EntityHuman entity, Location block)
    {
        // 交点を求める
        Vec3D start = new Vec3D(entity.locX, entity.locY + entity.getHeadHeight(), entity.locZ);
        Vec3D end = new Vec3D(block.getX(), block.getY(), block.getZ());

        MovingObjectPosition result = entity.world.rayTrace(new RayTrace(
                start, end,
                RayTrace.BlockCollisionOption.COLLIDER,
                RayTrace.FluidCollisionOption.ANY,
                entity
        ));
        if (result == null)
            return new TraceResult(0, 0, 0);

        return new TraceResult((float) result.getPos().x, (float) result.getPos().y, (float) result.getPos().z);
    }

    @Value
    public static class TraceResult
    {
        float x;
        float y;
        float z;
    }
}
