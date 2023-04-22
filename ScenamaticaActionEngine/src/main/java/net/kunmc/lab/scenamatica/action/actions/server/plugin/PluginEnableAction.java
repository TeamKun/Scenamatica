package net.kunmc.lab.scenamatica.action.actions.server.plugin;

import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PluginEnableAction extends AbstractPluginAction<PluginEnableAction.PluginEnableActionArgument> implements Requireable<PluginEnableAction.PluginEnableActionArgument>
{
    public static final String KEY_ACTION_NAME = KEY_PREFIX + "enable";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PluginEnableAction.PluginEnableActionArgument argument)
    {
        if (argument == null)
            return;

        Bukkit.getPluginManager().enablePlugin(argument.getPlugin(engine));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PluginEnableEvent.class
        );
    }

    @Override
    public PluginEnableActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        return new PluginEnableActionArgument(super.deserializePlugin(map));
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PluginEnableActionArgument argument, @NotNull ScenarioEngine engine)
    {
        argument = super.requireArgsNonNull(argument);
        return argument.getPlugin(engine).isEnabled();
    }

    @Override
    public void validateArgument(@Nullable PluginEnableActionArgument argument)
    {

    }

    public static class PluginEnableActionArgument extends AbstractPluginActionArgument
    {
        public PluginEnableActionArgument(String plugin)
        {
            super(plugin);
        }

        @Override
        public String getArgumentString()
        {
            return "plugin=" + this.getPluginName();
        }
    }
}
