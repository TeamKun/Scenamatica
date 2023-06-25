package org.kunlab.scenamatica.interfaces.scenariofile.inventory;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * プレイヤのインベントリの定義を表すインターフェースです。
 */
public interface PlayerInventoryBean extends InventoryBean, Serializable
{
    String KEY_MAIN_INVENTORY = "main";
    String KEY_MAIN_HAND = "mainHandItem";
    String KEY_OFF_HAND = "offHandItem";
    String KEY_ARMOR_CONTENTS = "armors";

    /**
     * メインハンドのアイテムを取得します。
     *
     * @return メインハンドのアイテム
     */
    @Nullable
    ItemStackBean getMainHand();

    /**
     * オフハンドのアイテムを取得します。
     *
     * @return オフハンドのアイテム
     */
    @Nullable
    ItemStackBean getOffHand();

    /**
     * 防具を取得します。
     * 配列のインデックスは以下の通りです。
     * <ul>
     *     <li>0 - ヘルメット</li>
     *     <li>1 - チェストプレート</li>
     *     <li>2 - レギンス</li>
     *     <li>3 - ブーツ</li>
     * </ul>
     *
     * @return 防具
     */
    @Nullable
    ItemStackBean[] getArmorContents();
}
