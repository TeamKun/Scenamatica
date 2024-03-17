package org.kunlab.scenamatica.nms.types.item;

import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.Versioned;

/**
 * ItemStack の NMS インターフェースです。
 */
public interface NMSItem extends NMSElement
{
    @Versioned(from = "1.16.5")
    int getMaxDurability();
}
