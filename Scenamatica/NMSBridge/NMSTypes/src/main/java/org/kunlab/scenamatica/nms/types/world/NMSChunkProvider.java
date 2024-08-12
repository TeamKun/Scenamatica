package org.kunlab.scenamatica.nms.types.world;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;

public interface NMSChunkProvider extends NMSElement
{
    /**
     * プレイヤチャンクマップを取得します。
     */
    void purgeUnload();

    /**
     * ワールドからエンティティを削除します。
     * @param entity 削除するエンティティ
     */
    @Versioned(from = "1.14")
    void removeEntity(@NotNull NMSEntity entity);
}
