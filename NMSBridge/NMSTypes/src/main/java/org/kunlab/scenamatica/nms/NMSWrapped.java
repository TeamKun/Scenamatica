package org.kunlab.scenamatica.nms;

/**
 * ラップされたNMSクラスを表すインターフェースです。
 */
public interface NMSWrapped
{
    /**
     * ラップされた NMS クラスを取得します。
     *
     * @return ラップされた NMS クラス
     */
    Object getNMSRaw();

    /**
     * ラップされた CraftBukkit クラスを取得します。
     *
     * @return ラップされた CraftBukkit クラス
     */
    Object getNMSCraftRaw();
}
