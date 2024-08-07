package org.kunlab.scenamatica.nms.enums.entity;

import org.kunlab.scenamatica.nms.NMSElement;

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
     * プレイヤによる移動です。
     */
    PLAYER,
    /**
     * プレイヤによる移動です。
     */
    PISTON,
    /**
     * シュルカーボックスによる移動です。
     */
    SHULKER_BOX,
    /**
     * シュルカー自身の移動です。
     */
    SHULKER
}
