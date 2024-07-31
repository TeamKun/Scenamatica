package org.kunlab.scenamatica.nms.types.player;

import org.kunlab.scenamatica.nms.NMSWrapped;

/**
 * PlayerConnection のラッパです。
 */
public interface NMSPlayerConnection extends NMSWrapped
{
    @Override
    default Object getBukkit()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * プレイヤとの接続を切断します。
     *
     * @param reason 切断理由
     */
    void disconnect(String reason);
}
