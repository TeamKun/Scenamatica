package org.kunlab.scenamatica.nms.impl.v1_20_R4.entity;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_20_R4.TypeSupportImpl;
import org.kunlab.scenamatica.nms.impl.v1_20_R4.item.NMSItemStackImpl;
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
        return new NMSItemStackImpl(this.nmsEntity.b(TypeSupportImpl.toNMS(slot)));
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
        EntityItem dropped = this.nmsEntity.a(
                this.nmsEntity.fq().a(
                        this.nmsEntity.fq().k,
                        (dropAll && !this.nmsEntity.fq().f().b()) ? this.nmsEntity.fq().f().I(): 1
                ),
                false,
                true
        );

        return dropped != null;
    }

    @Override
    public int getFoodLevel()
    {
        return this.nmsEntity.fz().a();
    }

    @Override
    public void setFoodLevel(int foodLevel)
    {
        this.nmsEntity.fz().a(foodLevel);
    }

    @Override
    public void setMainHand(MainHand hand)
    {
        this.nmsEntity.a(hand == MainHand.RIGHT ? EnumMainHand.a: EnumMainHand.b);
    }

    @Override
    public Player getNMSRaw()
    {
        return this.nmsEntity;
    }
}
