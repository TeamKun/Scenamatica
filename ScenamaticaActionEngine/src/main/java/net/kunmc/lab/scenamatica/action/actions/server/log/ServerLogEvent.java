package net.kunmc.lab.scenamatica.action.actions.server.log;

import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * サーバのログが出力されたときに呼び出されるイベントです。
 */
@Getter
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

    public ServerLogEvent(@NotNull String sender, @NotNull String message, @NotNull Level level)
    {
        super(true);
        this.sender = sender;
        this.message = message;
        this.level = level;
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

}
