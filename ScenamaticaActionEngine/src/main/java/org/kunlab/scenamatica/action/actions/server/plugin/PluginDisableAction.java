package org.kunlab.scenamatica.action.actions.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PluginDisableAction extends AbstractPluginAction<PluginDisableAction.Argument>
        implements Requireable<PluginDisableAction.Argument>, Watchable<PluginDisableAction.Argument>
{
    public static final String KEY_ACTION_NAME = KEY_PREFIX + "disable";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PluginDisableAction.Argument argument)
    {
        if (argument == null)
            return;
        else if (engine.getPlugin().getName().equalsIgnoreCase(argument.getPlugin().getName()))
            throw new IllegalArgumentException("Cannot disable the plugin itself.");

        Bukkit.getPluginManager().disablePlugin(argument.getPlugin());
    }

    @Override
    public boolean isFired(Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
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
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(super.deserializePlugin(map));
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PluginDisableAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = super.requireArgsNonNull(argument);
        return !argument.getPlugin().isEnabled();
    }

    public static class Argument extends AbstractPluginActionArgument
    {
        public Argument(String plugin)
        {
            super(plugin);
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_PLUGIN, this.getPluginName()
            );
        }
    }

}
