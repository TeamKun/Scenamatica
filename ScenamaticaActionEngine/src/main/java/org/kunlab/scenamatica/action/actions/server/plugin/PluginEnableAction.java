package org.kunlab.scenamatica.action.actions.server.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PluginEnableAction extends AbstractPluginAction<PluginEnableAction.Argument>
        implements Executable<PluginEnableAction.Argument>, Watchable<PluginEnableAction.Argument>, Requireable<PluginEnableAction.Argument>
{
    public static final String KEY_ACTION_NAME = KEY_PREFIX + "enable";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull PluginEnableAction.Argument argument)
    {
        if (argument == null)
            return;

        Bukkit.getPluginManager().enablePlugin(argument.getPlugin());
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
                PluginEnableEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(super.deserializePlugin(map));
    }

    @Override
    public boolean isConditionFulfilled(@NotNull PluginEnableAction.Argument argument, @NotNull ScenarioEngine engine)
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
