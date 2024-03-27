package org.kunlab.scenamatica.nms.impl.v1_16_R3.item;

import net.minecraft.server.v1_16_R3.Item;
import org.kunlab.scenamatica.nms.types.item.NMSItem;

public class NMSItemImpl implements NMSItem
{
    private final Item nmsItem;

    public NMSItemImpl(Item nmsItem)
    {
        this.nmsItem = nmsItem;
    }

    @Override
    public int getMaxDurability()
    {
        return this.nmsItem.getMaxDurability();
    }
}
