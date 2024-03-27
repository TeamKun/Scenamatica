package org.kunlab.scenamatica.nms.types.item;

import org.kunlab.scenamatica.nms.NMSElement;

/**
 * Item の NMS インターフェースです。
 */
public interface NMSItem extends NMSElement
{
    int getMaxDurability();
}
