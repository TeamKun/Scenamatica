package net.kunmc.lab.scenamatica.action.actions.scenamatica.plugin;

import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PluginDisableAction extends AbstractPluginAction<PluginDisableAction.PluginDisableActionArgument> implements Requireable<PluginDisableAction.PluginDisableActionArgument>
{
    public static final String KEY_ACTION_NAME = "plugin_disable";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PluginDisableAction.PluginDisableActionArgument argument)
    {
        if (argument == null)
            return;

        Bukkit.getPluginManager().disablePlugin(argument.getPlugin(engine));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PluginDisableEvent.class
        );
    }

    @Override
    public PluginDisableActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        return new PluginDisableActionArgument(super.deserializePlugin(map));
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PluginDisableAction.PluginDisableActionArgument argument, @NotNull ScenarioEngine engine)
    {
        argument = super.requireArgsNonNull(argument);
        return argument.getPlugin(engine).isEnabled();
    }

    @Override
    public void validateArgument(@Nullable PluginDisableAction.PluginDisableActionArgument argument)
    {

    }

    public static class PluginDisableActionArgument extends AbstractPluginActionArgument
    {
        public PluginDisableActionArgument(String plugin)
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
