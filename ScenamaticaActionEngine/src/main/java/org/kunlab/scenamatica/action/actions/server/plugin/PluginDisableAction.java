package org.kunlab.scenamatica.action.actions.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Plugin plugin = super.getPlugin(argument);
        if (engine.getPlugin() == plugin)
            throw new IllegalArgumentException("Cannot disable the plugin itself.");

        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    @Override
    public boolean isFired(InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        return this.checkMatchedPluginEvent(argument, engine, event);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PluginDisableEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        return !super.getPlugin(argument).isEnabled();
    }


}
