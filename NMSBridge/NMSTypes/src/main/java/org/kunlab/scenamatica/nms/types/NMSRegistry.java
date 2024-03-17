package org.kunlab.scenamatica.nms.types;

import org.jetbrains.annotations.Contract;
import org.kunlab.scenamatica.nms.types.item.NMSItem;

/**
 * NMS の登録を管理するインターフェースです。
 */
public interface NMSRegistry
{
    @Contract("null -> null")
    NMSItem getItemByNMS(Object nms);
}
