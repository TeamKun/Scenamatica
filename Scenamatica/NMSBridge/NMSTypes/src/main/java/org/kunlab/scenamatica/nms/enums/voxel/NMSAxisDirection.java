package org.kunlab.scenamatica.nms.enums.voxel;

import lombok.Getter;

/**
 * 軸の向きを表す列挙型です。
 */
@Getter
public enum NMSAxisDirection
{
    /**
     * 正の方向に向いています。
     */
    POSITIVE("Towards positive"),
    /**
     * 負の方向に向いています。
     */
    NEGATIVE("Towards negative");

    private final String description;

    NMSAxisDirection(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return this.description;
    }

    /**
     * 逆の方向を取得します。
     *
     * @return 逆の方向
     */
    public NMSAxisDirection opposite()
    {
        return this == POSITIVE ? NEGATIVE: POSITIVE;
    }
}
