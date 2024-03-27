package org.kunlab.scenamatica.events.actor;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.context.Actor;

/**
 * アクターが参加した後に呼び出されるイベントです。
 */
public class ActorPostJoinEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final Actor actor;

    public ActorPostJoinEvent(Actor actor)
    {
        this.actor = actor;
    }

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
