package org.kunlab.scenamatica.nms.types.world;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;

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
     *
     * @return チャンクプロバイダ
     */
    @NotNull
    NMSChunkProvider getChunkProvider();

    /**
     * エンティティマネージャを取得します。
     *
     * @return エンティティマネージャ
     */
    @NotNull
    @Versioned(from = "1.17")
    NMSPersistentEntitySectionManager<Entity> getEntityManager();

    /**
     * ブロックのアクションを再生します。
     * e.g. 音ブロックの再生
     *
     * @param block  ブロック
     * @param param1 ブロック固有パラメータ１
     * @param param2 ブロック固有パラメータ２
     */
    void playBlockAction(Block block, int param1, int param2);
}
