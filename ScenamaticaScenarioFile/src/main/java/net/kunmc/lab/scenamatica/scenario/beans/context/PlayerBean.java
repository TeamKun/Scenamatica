package net.kunmc.lab.scenamatica.scenario.beans.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.scenario.beans.inventory.InventoryBean;
import net.kunmc.lab.scenamatica.scenario.beans.inventory.PlayerInventoryBean;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.UUID;

/**
 * 疑似プレイヤーを表すクラスです。
 */
@Value
public class PlayerBean implements Serializable
{
    /**
     * プレイヤーの名前です。
     */
    @Nullable
    String name;
    /**
     * プレイヤーのUUIDです。
     */
    @Nullable
    UUID uuid;
    /**
     * プレイヤーの座標です。
     */
    @Nullable
    Location location;
    /**
     * プレイヤーのインベントリです。
     */
    @Nullable
    PlayerInventoryBean inventory;

    /**
     * エンダーチェストの定義を表すクラスです。
     */
    @NotNull
    InventoryBean enderChest;
}
