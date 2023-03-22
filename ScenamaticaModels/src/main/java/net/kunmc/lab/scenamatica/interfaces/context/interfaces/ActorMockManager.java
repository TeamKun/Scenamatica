package net.kunmc.lab.scenamatica.interfaces.context.interfaces;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.entity.Player;

public interface ActorMockManager
{
    Player mock(PlayerBean bean);

    void unmock(Player player);

    void shutdown();
}
