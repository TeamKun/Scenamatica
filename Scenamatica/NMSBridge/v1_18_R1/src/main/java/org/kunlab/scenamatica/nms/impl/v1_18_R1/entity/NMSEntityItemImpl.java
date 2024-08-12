package org.kunlab.scenamatica.nms.impl.v1_18_R1.entity;

import net.minecraft.world.entity.item.EntityItem;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.Item;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;

public class NMSEntityItemImpl extends NMSEntityImpl implements NMSEntityItem
{
    EntityItem nmsItem;
    Item bukkitItem;

    public NMSEntityItemImpl(EntityItem nmsItem)
    {
        super(nmsItem.getBukkitEntity());
        this.nmsItem = nmsItem;
        this.bukkitItem = (Item) nmsItem.getBukkitEntity();
    }

    public NMSEntityItemImpl(Item bukkitItem)
    {
        super(bukkitItem);
        this.nmsItem = (EntityItem) ((CraftEntity) bukkitItem).getHandle();
        this.bukkitItem = bukkitItem;
    }

    @Override
    public EntityItem getNMSRaw()
    {
        return this.nmsItem;
    }

    @Override
    public Item getBukkit()
    {
        return this.bukkitItem;
    }
}
