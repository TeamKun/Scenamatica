package org.kunlab.scenamatica.nms.impl.v1_20_R3.item;

import net.minecraft.world.item.Item;
import org.kunlab.scenamatica.nms.types.item.NMSItem;

public class NMSItemImpl implements NMSItem
{
    private final Item nmsItem;

    public NMSItemImpl(Item nmsItem)
    {
        this.nmsItem = nmsItem;
    }

}
