package org.kunlab.scenamatica.events;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.reporter.BukkitTestReporter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventListener implements Listener
{
    private final ActorManager actorManager;
    private final BukkitTestReporter testReporter;

    public PlayerJoinEventListener(ScenamaticaRegistry registry)
    {
        this.actorManager = registry.getContextManager().getActorManager();
        assert registry.getTestReporter() instanceof BukkitTestReporter;
        this.testReporter = (BukkitTestReporter) registry.getTestReporter();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!this.actorManager.isActor(event.getPlayer())) // 別に送信してもいいが, 他のテストに影響しそうなのでしない。
            this.testReporter.addRecipient(Terminals.of(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerJoinEvent event)
    {
        if (!this.actorManager.isActor(event.getPlayer()))
            this.testReporter.removeRecipient(Terminals.of(event.getPlayer()));
    }
}
