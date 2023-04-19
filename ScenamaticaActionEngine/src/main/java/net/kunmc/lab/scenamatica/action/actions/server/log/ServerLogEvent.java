package net.kunmc.lab.scenamatica.action.actions.server.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * サーバのログが出力されたときに呼び出されるイベントです。
 */
@Getter
@AllArgsConstructor
public class ServerLogEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();
    /**
     * ログを送信したものの識別子です。
     */
    @NotNull
    private final String sender;
    /**
     * ログの内容です。
     */
    @NotNull
    private final String message;
    /**
     * ログのレベルです。
     */
    @NotNull
    private final Level level;

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

}
