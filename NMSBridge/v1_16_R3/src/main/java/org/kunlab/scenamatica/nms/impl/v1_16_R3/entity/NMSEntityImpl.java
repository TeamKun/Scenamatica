package org.kunlab.scenamatica.nms.impl.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityItem;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.TypeSupportImpl;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.utils.NMSSupport;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

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
    public Entity getNMSRaw()
    {
        return this.nmsEntity;
    }

    @Override
    public org.bukkit.entity.Entity getBukkit()
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

    @Override
    public NMSEntityItem dropItem(@NotNull NMSItemStack stack, float offsetY)
    {
        EntityItem dropped = this.nmsEntity.a((ItemStack) stack.getNMSRaw(), offsetY);
        if (dropped == null)
            return null;

        return new NMSEntityItemImpl(dropped);
    }
}
