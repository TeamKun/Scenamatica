package org.kunlab.scenamatica.action.actions.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.server.AbstractServerAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

public abstract class AbstractPluginAction extends AbstractServerAction
{
    public static final InputToken<String> IN_PLUGIN = ofInput(
            "plugin",
            String.class
    );
    protected static final String KEY_PREFIX = "server_plugin_";

    public boolean checkMatchedPluginEvent(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof PluginEvent))
            return false;

        PluginEvent e = (PluginEvent) event;

        return argument.ifPresent(IN_PLUGIN, plugin -> plugin.equalsIgnoreCase(e.getPlugin().getName()));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_PLUGIN);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board = board.requirePresent(IN_PLUGIN);

        return board;
    }

    @NotNull
    protected Plugin getPlugin(InputBoard argument)
    {
        String pluginName = argument.get(IN_PLUGIN);

        Plugin plugin;
        if ((plugin = Bukkit.getPluginManager().getPlugin(pluginName)) == null)
            throw new IllegalArgumentException("Plugin not found: " + pluginName);

        return plugin;
    }
}
