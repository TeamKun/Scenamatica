package org.kunlab.scenamatica.events;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.util.logging.Logger;

public class PluginEventListener implements Listener
{
    private final ScenamaticaRegistry registry;
    private final Logger logger;

    public PluginEventListener(ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.logger = registry.getLogger();
    }

    @EventHandler
    public void onPluginEnabled(PluginEnableEvent event)
    {
        MsgArgs msgArgs = MsgArgs.of("plugin", event.getPlugin().getName());
        this.logger.info(LangProvider.get("daemon.plugin.enabled", msgArgs));
        this.logger.info(LangProvider.get("daemon.plugin.scenario.scanning", msgArgs));

        try
        {
            this.registry.getScenarioManager().loadPluginScenarios(event.getPlugin());
            this.logger.info(LangProvider.get("daemon.plugin.scenario.load.success", msgArgs));
        }
        catch (Exception e)
        {
            this.logger.warning(LangProvider.get("daemon.plugin.scenario.load.failed", msgArgs));
            this.registry.getExceptionHandler().report(e);
        }

    }

    @EventHandler
    public void onPluginDisabled(PluginDisableEvent event)
    {
        this.safePluginShutdown(event.getPlugin());
        MsgArgs msgArgs = MsgArgs.of("plugin", event.getPlugin().getName());
        this.logger.info(LangProvider.get("daemon.plugin.disabled", msgArgs));
        this.logger.info(LangProvider.get("daemon.plugin.scenario.unloading", msgArgs));

        this.registry.getScenarioManager().unloadPluginScenarios(event.getPlugin());
    }

    private void safePluginShutdown(Plugin plugin)
    {
        this.registry.getScenarioManager().dequeueScenarios(plugin);
    }
}
