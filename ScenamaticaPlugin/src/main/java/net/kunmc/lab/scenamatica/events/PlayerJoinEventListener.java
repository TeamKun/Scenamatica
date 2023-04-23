package net.kunmc.lab.scenamatica.events;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.reporter.BukkitTestReporter;
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
