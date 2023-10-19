package org.kunlab.scenamatica.interfaces.context;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.UUID;

/**
 * アクターのインターフェースです。
 */
public interface Actor
{
    /**
     * アクターマネージャを取得します。
     *
     * @return アクターマネージャ
     */
    @NotNull
    ActorManager getManager();

    /**
     * アニメーションを再生します。
     *
     * @param animation 再生するアニメーション
     */
    void playAnimation(@NotNull PlayerAnimationType animation);

    /**
     * ブロックまたは空中をクリックします。
     *
     * @param action クリックするアクション
     * @param block  クリックするブロック
     */
    void interactAt(@NotNull Action action, Block block);

    /**
     * エンティティをクリックします。
     *
     * @param entity   クリックするエンティティ
     * @param type     クリックするアクション
     * @param hand     クリックする手
     * @param location クリックする位置
     */
    void interactEntity(@NotNull Entity entity, @NotNull NMSEntityUseAction type, @Nullable EquipmentSlot hand,
                        @Nullable Location location);

    /**
     * エンティティのアイテムを設置します。
     *
     * @param location 設置するアイテムの位置
     * @param item     設置するアイテム
     */
    void placeItem(@NotNull Location location, @NotNull ItemStack item);

    /**
     * エンティティのアイテムを設置します。
     *
     * @param location 設置するアイテムの位置
     * @param item     設置するアイテム
     */
    void placeItem(@NotNull Location location, @NotNull ItemStack item, @Nullable BlockFace face);

    /**
     * ブロックを破壊します。
     *
     * @param block 破壊するブロック
     */
    void breakBlock(Block block);

    /**
     * ブロックを設置します。
     *
     * @param location  設置するブロックの位置
     * @param stack     設置するブロックのアイテム
     * @param slot      プレイヤの手( {@link EquipmentSlot#HAND} または {@link EquipmentSlot#OFF_HAND} )
     * @param direction プレイヤの向き
     */
    void placeBlock(@NotNull Location location, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull BlockFace direction);

    /**
     * サーバに参加します。
     *
     * @throws IllegalStateException すでに参加している場合
     * @see Player#isOnline()
     */
    void joinServer();

    /**
     * サーバから退出します。
     *
     * @throws IllegalStateException 参加していない場合
     * @see Player#isOnline()
     */
    void leaveServer();

    /**
     * サーバからタイムアウトしてキックされます。
     */
    void kickTimeout();

    /**
     * サーバからエラーでキックされます。
     */
    void kickErroneous();

    /**
     * アイテムを食べます。
     *
     * @param slot 食べるアイテムの手( {@link EquipmentSlot#HAND} または {@link EquipmentSlot#OFF_HAND} )
     * @throws IllegalStateException アイテムを持っていない場合/アイテムが食べられない場合
     */
    void consume(@NotNull EquipmentSlot slot);

    /**
     * 持ってるツールを壊します。
     *
     * @param slot 壊すツールの手( {@link EquipmentSlot#HAND} または {@link EquipmentSlot#OFF_HAND} )
     */
    void breakItem(@NotNull EquipmentSlot slot);

    /**
     * 持ってるアイテムにダメージを与えます。
     *
     * @param slot   手
     * @param damage ダメージ
     */
    void damageItem(@NotNull EquipmentSlot slot, int damage);

    void giveCreativeItem(int slot, @NotNull ItemStack item);

    /**
     * Bukkit の {@link Player} を取得します。
     *
     * @return プレイヤー
     */
    @NotNull
    Player getPlayer();

    /**
     * {@link UUID} を取得します。
     *
     * @return プレイヤーのUUID
     */
    @NotNull
    UUID getUUID();

    /**
     * 名前を取得します。
     *
     * @return アクターの名前
     */
    @NotNull
    String getName();

    void clickInventory(@NotNull ClickType type,
                        int slot,
                        int button,
                        @Nullable ItemStack clickedItem);
}
