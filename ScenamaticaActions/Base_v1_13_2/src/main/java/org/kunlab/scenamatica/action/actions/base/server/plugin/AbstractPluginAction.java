package org.kunlab.scenamatica.action.actions.base.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.server.AbstractServerAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;

public abstract class AbstractPluginAction extends AbstractServerAction
{
    public static final InputToken<String> IN_PLUGIN = ofInput(
            "plugin",
            String.class
    );
    public static final String KEY_OUT_PLUGIN = "plugin";

    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof PluginEvent))
            return false;

        PluginEvent e = (PluginEvent) event;

        boolean result = ctxt.ifHasInput(IN_PLUGIN, plugin -> plugin.equalsIgnoreCase(e.getPlugin().getName()));
        if (result)
            this.makeOutputs(ctxt, e.getPlugin());

        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_PLUGIN);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board = board.requirePresent(IN_PLUGIN);

        return board;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Plugin plugin)
    {
        ctxt.output(KEY_OUT_PLUGIN, plugin.getName());
        ctxt.commitOutput();
    }

    @NotNull
    protected Plugin getPlugin(ActionContext ctxt)
    {
        String pluginName = ctxt.input(IN_PLUGIN);

        Plugin plugin;
        if ((plugin = Bukkit.getPluginManager().getPlugin(pluginName)) == null)
            throw new IllegalArgumentException("Plugin not found: " + pluginName);

        return plugin;
    }
}
