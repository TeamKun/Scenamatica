package org.kunlab.scenamatica.action.actions.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

public class PluginDisableAction extends AbstractPluginAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = KEY_PREFIX + "disable";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Plugin plugin = super.getPlugin(ctxt);
        if (ctxt.getEngine().getPlugin() == plugin)
            throw new IllegalArgumentException("Cannot disable the plugin itself.");

        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        return this.checkMatchedPluginEvent(ctxt, event);
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
        return !super.getPlugin(ctxt).isEnabled();
    }


}
