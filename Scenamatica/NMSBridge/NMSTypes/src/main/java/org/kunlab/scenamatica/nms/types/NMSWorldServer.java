package org.kunlab.scenamatica.nms.types;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.NMSWrapped;

/**
 * WorldServer の NMS による実装を提供します。
 */
public interface NMSWorldServer extends NMSWrapped
{
    @Override
    World getBukkit();

    /**
     * ワールドデータを取得します。
     *
     * @return ワールドデータ
     */
    @NotNull
    NMSWorldData getWorldData();

    /**
     * チャンクプロバイダを取得します。
     * @return チャンクプロバイダ
     */
    @NotNull
    NMSChunkProvider getChunkProvider();
}
