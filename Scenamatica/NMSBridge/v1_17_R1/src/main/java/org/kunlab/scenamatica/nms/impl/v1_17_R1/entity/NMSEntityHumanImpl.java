package org.kunlab.scenamatica.nms.impl.v1_17_R1.entity;

import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.TypeSupportImpl;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.item.NMSItemStackImpl;
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
        EntityItem dropped = this.nmsEntity.a(
                this.nmsEntity.getInventory().splitStack(
                        this.nmsEntity.getInventory().k,
                        dropAll && !this.nmsEntity.getInventory().getItemInHand().isEmpty() ? this.nmsEntity.getInventory().getItemInHand().getCount(): 1
                ),
                false,
                true
        );

        return dropped != null;
    }

    @Override
    public int getFoodLevel()
    {
        return this.nmsEntity.getFoodData().getFoodLevel();
    }

    @Override
    public void setFoodLevel(int foodLevel)
    {
        this.nmsEntity.getFoodData().a = foodLevel;
    }

    @Override
    public void setMainHand(MainHand hand)
    {
        this.nmsEntity.a(hand == MainHand.RIGHT ? EnumMainHand.a: EnumMainHand.b);
    }

    @Override
    public EntityHuman getNMSRaw()
    {
        return this.nmsEntity;
    }
}
