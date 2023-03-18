package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * プレイヤのインベントリの定義を表すクラスです。
 */
@Value
public class PlayerInventoryBean implements Serializable
{
    /**
     * メインインベントリの定義を表すクラスです。
     */
    @NotNull
    InventoryBean mainInventory;

    /**
     * オフハンドの定義を表すクラスです。
     */
    @Nullable
    ItemStackBean offHand;

    /**
     * アーマーの定義を表すクラスです。
     */
    @NotNull
    ItemStackBean[] armorContents;


}
