package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;

/**
 * インベントリの定義を表すクラスです。
 */
public class InventoryBean implements Serializable
{
    /**
     * インベントリのサイズです。
     */
    int size;
    /**
     * インベントリのタイトルです。
     */
    @Nullable
    String title;
    /**
     * インベントリのアイテムです。
     */
    @NotNull
    Map<Integer, itemStackBean> mainContents;

}
