package org.kunlab.scenamatica.nms.impl.v1_13_R2.item;

import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.ItemStack;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.impl.v1_13_R2.NMSRegistryImpl;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.types.item.NMSItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

import java.lang.reflect.Field;

public class NMSItemStackImpl implements NMSItemStack
{
    private static final Field fHANDLE; // Lorg/bukkit/craftbukkit/v1_13_R2/inventory/CraftItemStack;
    // -> handle:Lnet/minecraft/server/v1_13_R2/ItemStack;

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
    public <T extends NMSEntityLiving> @Versioned void damage(int damage, T owner)
    {
        this.nmsItemStack.damage(
                damage,
                (EntityLiving) owner.getNMSRaw()
        );
    }

    @Override
    public NMSItem getItem()
    {
        return NMSRegistryImpl.getItemByNMS$(this.nmsItemStack.getItem());
    }
}
