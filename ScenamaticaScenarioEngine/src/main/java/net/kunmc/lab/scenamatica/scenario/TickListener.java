package net.kunmc.lab.scenamatica.scenario;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import lombok.AllArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
        /* non-public */ class TickListener implements Listener
{
    private final ScenamaticaRegistry registry;
    private final ScenarioManager manager;

    /* non-public */ void init()
    {
        this.registry.getPlugin().getServer().getPluginManager().registerEvents(this, this.registry.getPlugin());
    }

    @EventHandler
    public void onTick(ServerTickEndEvent event)
    {
        if (this.manager.isRunning())
        {
            ScenarioEngine currentEngine = this.manager.getCurrentScenario();
            assert currentEngine != null;
            currentEngine.onTick();
        }
    }
}
