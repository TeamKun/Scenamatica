package net.kunmc.lab.scenamatica.events;

import lombok.Getter;
import net.kunmc.lab.scenamatica.interfaces.scenario.MilestoneEntry;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * マイルストーンが達成されたときに呼び出されるイベント
 */
@Getter
public class MilestoneReachedEvent extends Event implements Cancellable
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * 到達したマイルストーンのエントリです。
     * 注意：エントリの {@link MilestoneEntry#isReached()} は、まだ更新されていません。
     */
    @NotNull
    private final MilestoneEntry milestone;

    private boolean cancelled;

    public MilestoneReachedEvent(@NotNull MilestoneEntry milestone)
    {
        super(/* isAsync: */ true);
        this.milestone = milestone;
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

    @Override
    public boolean isCancelled()
    {
        return this.milestone.isReached();
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        this.milestone.setReached(cancel);
    }
}
