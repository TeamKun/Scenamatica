package org.kunlab.scenamatica.nms.impl.v1_16_R3.item;

import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.ItemStack;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.NMSRegistryImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.types.item.NMSItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

import java.util.function.Consumer;

public class NMSItemStackImpl implements NMSItemStack
{
    private final ItemStack nmsItemStack;
    private final org.bukkit.inventory.ItemStack bukkitItemStack;

    public NMSItemStackImpl(org.bukkit.inventory.ItemStack bukkitItemStack)
    {
        this.bukkitItemStack = bukkitItemStack;
        this.nmsItemStack = CraftItemStack.asNMSCopy(bukkitItemStack);
    }

    public NMSItemStackImpl(ItemStack nmsItemStack)
    {
        this.nmsItemStack = nmsItemStack;
        this.bukkitItemStack = CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public Object getNMSRaw()
    {
        return this.nmsItemStack;
    }

    @Override
    public org.bukkit.inventory.ItemStack getBukkit()
    {
        return this.bukkitItemStack;
    }

    @Override
    public <T extends NMSEntityLiving> void damage(int damage, T owner, Consumer<T> onBreak)
    {
        this.nmsItemStack.damage(
                damage,
                (EntityLiving) owner.getNMSRaw(),
                (entity) -> onBreak.accept(owner)
        );
    }

    @Override
    public NMSItem getItem()
    {
        return NMSRegistryImpl.getItemByNMS$(this.nmsItemStack.getItem());
    }
}
