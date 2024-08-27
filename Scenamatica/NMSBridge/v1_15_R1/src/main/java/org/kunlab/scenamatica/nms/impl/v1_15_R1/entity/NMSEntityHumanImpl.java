package org.kunlab.scenamatica.nms.impl.v1_15_R1.entity;

import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityItem;
import net.minecraft.server.v1_15_R1.EnumMainHand;
import net.minecraft.server.v1_15_R1.ItemStack;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_15_R1.TypeSupportImpl;
import org.kunlab.scenamatica.nms.impl.v1_15_R1.item.NMSItemStackImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityHuman;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

public class NMSEntityHumanImpl extends NMSEntityLivingImpl implements NMSEntityHuman
{
    private final HumanEntity bukkitEntity;
    private final EntityHuman nmsEntity;

    public NMSEntityHumanImpl(HumanEntity bukkitEntity)
    {
        super(bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        this.nmsEntity = ((CraftHumanEntity) bukkitEntity).getHandle();
    }

    @Override
    public HumanEntity getBukkit()
    {
        return this.bukkitEntity;
    }

    @Override
    public NMSItemStack getEquipment(NMSItemSlot slot)
    {
        return new NMSItemStackImpl(this.nmsEntity.getEquipment(TypeSupportImpl.toNMS(slot)));
    }

    @Override
    public NMSEntityItem drop(@NotNull NMSItemStack stack, boolean throwRandomly, boolean retainOwnership)
    {
        EntityItem dropped = this.nmsEntity.a((ItemStack) stack.getNMSRaw(), throwRandomly, retainOwnership);
        if (dropped == null)
            return null;
        return new NMSEntityItemImpl(dropped);
    }

    @Override
    public boolean drop(boolean dropAll)
    {
        return this.nmsEntity.n(dropAll);
    }

    @Override
    public int getFoodLevel()
    {
        return this.nmsEntity.getFoodData().getFoodLevel();
    }

    @Override
    public void setFoodLevel(int foodLevel)
    {
        this.nmsEntity.getFoodData().foodLevel = foodLevel;
    }

    @Override
    public void setMainHand(MainHand hand)
    {
        this.nmsEntity.a(hand == MainHand.RIGHT ? EnumMainHand.LEFT: EnumMainHand.RIGHT);
    }

    @Override
    public EntityHuman getNMSRaw()
    {
        return this.nmsEntity;
    }
}
