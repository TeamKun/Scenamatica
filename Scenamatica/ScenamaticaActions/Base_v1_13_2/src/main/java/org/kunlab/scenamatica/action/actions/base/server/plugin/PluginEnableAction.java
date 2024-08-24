package org.kunlab.scenamatica.action.actions.base.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.util.Collections;
import java.util.List;

@Action("server_plugin_enable")
@ActionDoc(
        name = "プラグインの有効化",
        description = "プラグインを有効化します。",
        events = {
                PluginEnableEvent.class
        },

        executable = "プラグインを有効化します。",
        expectable = "プラグインが有効化されることを期待します。",
        requireable = "プラグインが有効化されていることを要求します。"

)
public class PluginEnableAction extends AbstractPluginAction
        implements Executable, Expectable, Requireable
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
