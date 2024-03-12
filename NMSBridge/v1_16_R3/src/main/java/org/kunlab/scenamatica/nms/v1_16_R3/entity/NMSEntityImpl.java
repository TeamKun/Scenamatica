package org.kunlab.scenamatica.nms.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.v1_16_R3.TypeSupportImpl;
import org.kunlab.scenamatica.nms.v1_16_R3.utils.NMSSupport;

public class NMSEntityImpl implements NMSEntity
{
    private final org.bukkit.entity.Entity bukkitEntity;
    private final Entity nmsEntity;

    public NMSEntityImpl(org.bukkit.entity.Entity bukkitEntity)
    {
        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
    }

    @Override
    public Object getNMSRaw()
    {
        return this.nmsEntity;
    }

    @Override
    public Object getNMSCraftRaw()
    {
        return this.bukkitEntity;
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntity()
    {
        return this.bukkitEntity;
    }

    @Override
    public void move(NMSMoveType moveType, Location location)
    {
        EnumMoveType convertedMoveType = TypeSupportImpl.toNMS(moveType);
        Vec3D convertedLocation = NMSSupport.convertLocToVec3D(location);

        this.nmsEntity.move(convertedMoveType, convertedLocation);
    }
}
