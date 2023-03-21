package net.kunmc.lab.scenamatica.interfaces.context.interfaces;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.entity.Player;

public interface PlayerMockManager
{
    Player mockPlayer(PlayerBean bean);

    void unmock(Player player);

    void shutdown();
}
