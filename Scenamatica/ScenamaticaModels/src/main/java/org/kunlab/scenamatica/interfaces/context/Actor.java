package org.kunlab.scenamatica.interfaces.context;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.nms.enums.NMSHand;
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
     * 初期化に使用された構造体を取得します。
     *
     * @return 構造体
     */
    PlayerStructure getInitialStructure();

    /**
     * 初期位置を取得します。
     *
     * @return 初期位置
     */
    Location getInitialLocation();

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
    void interactEntity(@NotNull Entity entity, @NotNull NMSEntityUseAction type, @Nullable NMSHand hand,
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
     * @param hand      プレイヤの手
     * @param direction プレイヤの向き
     */
    void placeBlock(@NotNull Location location, @NotNull ItemStack stack, @NotNull NMSHand hand, @NotNull BlockFace direction);

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
     * クリエイティブモードでアイテムを与えます。
     *
     * @param slot スロット
     * @param item アイテム
     */
    void giveCreativeItem(int slot, @NotNull ItemStack item);

    /**
     * インベントリをクリックします。
     *
     * @param type        クリックタイプ
     * @param slot        クリックするスロット
     * @param button      クリックするボタン
     * @param clickedItem クリックするアイテム
     */
    void clickInventory(@NotNull ClickType type,
                        int slot,
                        int button,
                        @Nullable ItemStack clickedItem);

    /**
     * Bukkit の {@link Player} を取得します。
     *
     * @return プレイヤ
     */
    @NotNull
    Player getPlayer();

    /**
     * {@link UUID} を取得します。
     *
     * @return プレイヤのUUID
     */
    @NotNull
    UUID getUniqueID();

    /**
     * 名前を取得します。
     *
     * @return アクターの名前
     */
    @NotNull
    String getActorName();
}
