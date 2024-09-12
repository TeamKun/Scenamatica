package org.kunlab.scenamatica.nms.impl.v1_20_R3.entity;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_20_R3.TypeSupportImpl;
import org.kunlab.scenamatica.nms.impl.v1_20_R3.item.NMSItemStackImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityHuman;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

public class NMSEntityHumanImpl extends NMSEntityLivingImpl implements NMSEntityHuman
{
    private final HumanEntity bukkitEntity;
    private final Player nmsEntity;

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
        return new NMSItemStackImpl(this.nmsEntity.getItemBySlot(TypeSupportImpl.toNMS(slot)));
    }

    @Override
    public NMSEntityItem drop(@NotNull NMSItemStack stack, boolean throwRandomly, boolean retainOwnership)
    {
        ItemEntity dropped = this.nmsEntity.drop((ItemStack) stack.getNMSRaw(), throwRandomly, retainOwnership);
        if (dropped == null)
            return null;
        return new NMSEntityItemImpl(dropped);
    }

    @Override
    public boolean drop(boolean dropAll)
    {
        ItemEntity dropped = this.nmsEntity.drop(
                this.nmsEntity.getInventory().removeItem(
                        this.nmsEntity.getInventory().selected,
                        (dropAll && !this.nmsEntity.getInventory().getSelected().isEmpty()) ? this.nmsEntity.getInventory().getSelected().getCount(): 1
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
        this.nmsEntity.getFoodData().setFoodLevel(foodLevel);
    }

    @Override
    public void setMainHand(MainHand hand)
    {
        this.nmsEntity.setMainArm(hand == MainHand.RIGHT ? HumanoidArm.RIGHT: HumanoidArm.LEFT);
    }

    @Override
    public Player getNMSRaw()
    {
        return this.nmsEntity;
    }
}
