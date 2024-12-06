package org.kunlab.scenamatica.action.actions.base.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.util.Collections;
import java.util.List;

@Action("server_plugin_disable")
@ActionDoc(
        name = "プラグインの無効化",
        description = "プラグインを無効化します。",
        events = {
                PluginDisableEvent.class
        },

        executable = "プラグインを無効化します。",
        expectable = "プラグインが無効化されることを期待します。",
        requireable = "プラグインが無効化されていることを要求します。"
)
public class PluginDisableAction extends AbstractPluginAction
        implements Executable, Expectable, Requireable
{

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Plugin plugin = super.getPlugin(ctxt);
        if (ctxt.getEngine().getPlugin() == plugin)
            throw new IllegalActionInputException(IN_PLUGIN, "Cannot disable the plugin itself.");

        this.makeOutputs(ctxt, plugin);
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PluginDisableEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        boolean result = !this.getPlugin(ctxt).isEnabled();
        if (result)
            this.makeOutputs(ctxt, this.getPlugin(ctxt));

        return result;
    }


}
