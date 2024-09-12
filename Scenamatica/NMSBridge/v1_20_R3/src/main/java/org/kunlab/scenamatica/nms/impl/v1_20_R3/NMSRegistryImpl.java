package org.kunlab.scenamatica.nms.impl.v1_20_R3;

import net.minecraft.world.item.Item;
import org.kunlab.scenamatica.nms.impl.v1_20_R3.item.NMSItemImpl;
import org.kunlab.scenamatica.nms.types.NMSRegistry;
import org.kunlab.scenamatica.nms.types.item.NMSItem;

import java.util.HashMap;
import java.util.Map;

public class NMSRegistryImpl implements NMSRegistry
{
    private static final Map<Object, NMSItem> ITEMS;

    static
    {
        ITEMS = new HashMap<>();
    }

    public static NMSItem getItemByNMS$(final Object nms)
    {
        if (nms == null)
            return null;
        else if (!(nms instanceof Item))
            throw new IllegalArgumentException("nms must be an instance of net.minecraft.world.level.Item");

        if (ITEMS.containsKey(nms))
            return ITEMS.get(nms);
        else
        {
            final NMSItem item = new NMSItemImpl((Item) nms);
            ITEMS.put(nms, item);
            return item;

        }
    }

    @Override
    public NMSItem getItemByNMS(final Object nms)
    {
        return getItemByNMS$(nms);
    }
}
