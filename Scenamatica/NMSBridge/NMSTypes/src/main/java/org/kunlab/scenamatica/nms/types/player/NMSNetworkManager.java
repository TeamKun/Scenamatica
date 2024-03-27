package org.kunlab.scenamatica.nms.types.player;

import org.kunlab.scenamatica.nms.NMSWrapped;

import java.net.SocketAddress;

/**
 * NetworkManager の NMS 版です。
 */
public interface NMSNetworkManager extends NMSWrapped
{
    @Override
    default Object getBukkit()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * ソケットアドレスを取得します。
     *
     * @return ソケットアドレス
     */
    SocketAddress getSocketAddress();
}
