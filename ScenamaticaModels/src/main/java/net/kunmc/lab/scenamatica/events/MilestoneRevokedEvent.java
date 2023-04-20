package net.kunmc.lab.scenamatica.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.scenario.MilestoneEntry;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * マイルストーンが取り消されたときに呼び出されるイベントです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class MilestoneRevokedEvent extends Event
{
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 取り消しされるマイルストーンです。
     */
    @NotNull
    MilestoneEntry entry;

    public MilestoneRevokedEvent(@NotNull MilestoneEntry entry)
    {
        super(!Bukkit.isPrimaryThread());  // これをしないとエラーが出る
        this.entry = entry;
    }

    @NotNull
    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLERS;
    }
}
