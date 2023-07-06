package org.kunlab.scenamatica.action.actions.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.Requireable;
import org.kunlab.scenamatica.interfaces.action.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PluginEnableAction extends AbstractPluginAction<PluginEnableAction.Argument>
        implements Requireable<PluginEnableAction.Argument>, Watchable<PluginEnableAction.Argument>
{
    public static final String KEY_ACTION_NAME = KEY_PREFIX + "enable";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PluginEnableAction.Argument argument)
    {
        if (argument == null)
            return;

        Bukkit.getPluginManager().enablePlugin(argument.getPlugin());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PluginEnableEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(super.deserializePlugin(map));
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PluginEnableAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = super.requireArgsNonNull(argument);
        return argument.getPlugin().isEnabled();
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
