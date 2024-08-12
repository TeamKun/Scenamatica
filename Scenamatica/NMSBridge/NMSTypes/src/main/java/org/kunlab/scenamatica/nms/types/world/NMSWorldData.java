package org.kunlab.scenamatica.nms.types.world;

import org.kunlab.scenamatica.nms.NMSElement;

/**
 * WorldData の NMS による実装を提供します。
 */
public interface NMSWorldData extends NMSElement
{
    /**
     * ハードコアモードかどうかを取得します。
     *
     * @return ハードコアモードの場合は true、そうでない場合は false
     */
    boolean isHardcore();

    /**
     * ハードコアモードかどうかを設定します。
     *
     * @param hardcore ハードコアモードの場合は true、そうでない場合は false
     */
    void setHardcore(boolean hardcore);
}
