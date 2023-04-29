package net.kunmc.lab.scenamatica.interfaces.context;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationType;
import org.jetbrains.annotations.NotNull;

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
}
