package org.kunlab.scenamatica.nms.impl.v1_18_R1.entity;

import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.Item;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;

public class NMSEntityItemImpl extends NMSEntityImpl implements NMSEntityItem
{
    ItemEntity nmsItem;
    Item bukkitItem;

    public NMSEntityItemImpl(ItemEntity nmsItem)
    {
        super(nmsItem.getBukkitEntity());
        this.nmsItem = nmsItem;
        this.bukkitItem = (Item) nmsItem.getBukkitEntity();
    }

    public NMSEntityItemImpl(Item bukkitItem)
    {
        super(bukkitItem);
        this.nmsItem = (ItemEntity) ((CraftEntity) bukkitItem).getHandle();
        this.bukkitItem = bukkitItem;
    }

    @Override
    public ItemEntity getNMSRaw()
    {
        return this.nmsItem;
    }

    @Override
    public Item getBukkit()
    {
        return this.bukkitItem;
    }
}
