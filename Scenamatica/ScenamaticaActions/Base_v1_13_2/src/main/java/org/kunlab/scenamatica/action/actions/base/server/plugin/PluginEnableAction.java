package org.kunlab.scenamatica.action.actions.base.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@ActionMeta("server_plugin_enable")
public class PluginEnableAction extends AbstractPluginAction
        implements Executable, Watchable, Requireable
{
    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Plugin plugin = super.getPlugin(ctxt);
        if (ctxt.getEngine().getPlugin() == plugin)
            throw new IllegalArgumentException("Cannot disable the plugin itself.");
        else if (plugin.isEnabled())
            throw new IllegalArgumentException("Plugin is already enabled.");

        this.makeOutputs(ctxt, plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PluginEnableEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        boolean result = this.getPlugin(ctxt).isEnabled();
        if (result)
            this.makeOutputs(ctxt, this.getPlugin(ctxt));

        return result;
    }
}
