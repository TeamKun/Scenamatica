package org.kunlab.scenamatica.nms.enums.entity;

import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.TypeSupport;

/**
 * エンティティの使用(interact)の種類です。
 */
public enum NMSEntityUseAction implements NMSElement
{
    INTERACT,
    ATTACK,
    INTERACT_AT;

    public static NMSEntityUseAction fromNMS(Object nmsEnum, TypeSupport typeSupport)
    {
        return typeSupport.fromNMS(nmsEnum, NMSEntityUseAction.class);
    }
}
