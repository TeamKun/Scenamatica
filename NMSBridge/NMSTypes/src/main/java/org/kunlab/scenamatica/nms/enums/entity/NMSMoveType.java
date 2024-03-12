package org.kunlab.scenamatica.nms.enums.entity;

import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.TypeSupport;

/**
 * エンティティの移動のタイプです。
 */
public enum NMSMoveType implements NMSElement
{
    /**
     * 自発的な移動です。
     */
    SELF,
    /**
     * プレイヤーによる移動です。
     */
    PLAYER,
    /**
     * プレイヤーによる移動です。
     */
    PISTON,
    /**
     * シュルカーボックスによる移動です。
     */
    SHULKER_BOX,
    /**
     * シュルカー自身の移動です。
     */
    SHULKER;

    public static NMSMoveType fromNMS(Object nmsEnum, TypeSupport typeSupport)
    {
        return typeSupport.fromNMS(nmsEnum, NMSMoveType.class);
    }
}
