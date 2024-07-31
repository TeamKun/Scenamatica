package org.kunlab.scenamatica.events.actor;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * アクターにメッセージが送信されたときに呼び出されるイベントです。
 * システムのメッセージや, 他プレイヤのメッセージなどが, クライアントが見るメッセージと同じように取得できます。
 */
public class ActorMessageReceiveEvent extends PlayerEvent
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * メッセージの内容です。
     */
    @Getter
    private final Text message;
    /**
     * メッセージの種類です。
     * とりあえずプロトコル準拠です。
     */
    @Getter
    private final Type type;

    public ActorMessageReceiveEvent(@NotNull Player who, @NotNull Text message, @NotNull Type type)
    {
        super(who);
        this.message = message;
        this.type = type;
    }

    @NotNull
    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }

    /**
     * チャットメッセージの種類です。
     */
    public enum Type
    {
        /**
         * システムメッセージです。
         */
        SYSTEM,
        /**
         * プレイヤからのメッセージです。
         */
        PLAYER,
        /**
         * ゲーム情報です。
         */
        GAME_INFO,
    }
}
