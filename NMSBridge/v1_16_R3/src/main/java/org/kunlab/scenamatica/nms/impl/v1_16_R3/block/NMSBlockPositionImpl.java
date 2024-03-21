package org.kunlab.scenamatica.nms.impl.v1_16_R3.block;

import lombok.Getter;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Location;
import org.kunlab.scenamatica.nms.types.block.NMSBlockPosition;

@Getter
public class NMSBlockPositionImpl implements NMSBlockPosition
{
    private final BlockPosition blockPosition;
    private final Location normalizedLocation;

    public NMSBlockPositionImpl(Location location)
    {
        this.normalizedLocation = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
        this.blockPosition = new BlockPosition(
                this.normalizedLocation.getBlockX(),
                this.normalizedLocation.getBlockY(),
                this.normalizedLocation.getBlockZ()
        );
    }

    public NMSBlockPositionImpl(int x, int y, int z)
    {
        this.normalizedLocation = new Location(null, x, y, z);
        this.blockPosition = new BlockPosition(x, y, z);
    }

    @Override
    public Location getBukkit()
    {
        return this.normalizedLocation;
    }

    @Override
    public Object getNMSRaw()
    {
        return this.blockPosition;
    }

    @Override
    public int getX()
    {
        return this.blockPosition.getX();
    }

    @Override
    public int getY()
    {
        return this.blockPosition.getY();
    }

    @Override
    public int getZ()
    {
        return this.blockPosition.getZ();
    }
}
