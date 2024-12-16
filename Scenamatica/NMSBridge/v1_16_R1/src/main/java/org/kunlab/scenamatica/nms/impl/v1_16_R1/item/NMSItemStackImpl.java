package org.kunlab.scenamatica.nms.impl.v1_16_R1.item;

import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.ItemStack;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.impl.v1_16_R1.NMSRegistryImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.types.item.NMSItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

import java.lang.reflect.Field;

public class NMSItemStackImpl implements NMSItemStack
{
    private static final Field fHANDLE; // Lorg/bukkit/craftbukkit/v1_15_R1/inventory/CraftItemStack;
    // -> handle:Lnet/minecraft/server/v1_15_R1/ItemStack;

    static
    {
        try
        {
            fHANDLE = CraftItemStack.class.getDeclaredField("handle");
            fHANDLE.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private final ItemStack nmsItemStack;
    private final org.bukkit.inventory.ItemStack bukkitItemStack;

    public NMSItemStackImpl(org.bukkit.inventory.ItemStack bukkitItemStack)
    {
        this.bukkitItemStack = bukkitItemStack;
        this.nmsItemStack = getHandle(bukkitItemStack);
    }

    public NMSItemStackImpl(ItemStack nmsItemStack)
    {
        this.nmsItemStack = nmsItemStack;
        this.bukkitItemStack = CraftItemStack.asCraftMirror(nmsItemStack);
    }

    private static ItemStack getHandle(org.bukkit.inventory.ItemStack bukkitItemStack)
    {
        if (!(bukkitItemStack instanceof CraftItemStack))
            return getHandle(CraftItemStack.asCraftCopy(bukkitItemStack));

        try
        {
            return (ItemStack) fHANDLE.get(bukkitItemStack);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ItemStack getNMSRaw()
    {
        return this.nmsItemStack;
    }

    @Override
    public org.bukkit.inventory.ItemStack getBukkit()
    {
        return this.bukkitItemStack;
    }

    @Override
    public <T extends NMSEntityLiving> void damage(int damage, T owner, NMSItemSlot slot)
    {
        this.nmsItemStack.damage(
                damage,
                (EntityLiving) owner.getNMSRaw(),
                ignored -> {
                }
        );
    }

    @Override
    public NMSItem getItem()
    {
        return NMSRegistryImpl.getItemByNMS$(this.nmsItemStack.getItem());
    }
}
