package net.kunmc.lab.scenamatica.action.actions.server.plugin;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AbstractPluginAction<A extends AbstractPluginActionArgument> extends AbstractAction<A>
{
    protected static final String KEY_PREFIX = "server_plugin_";

    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof PluginEvent))
            return false;

        PluginEvent e = (PluginEvent) event;

        return e.getPlugin().getName().equalsIgnoreCase(argument.getPluginName());
    }

    protected String deserializePlugin(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractPluginActionArgument.KEY_PLUGIN);

        return map.get(AbstractPluginActionArgument.KEY_PLUGIN).toString();
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable A argument)
    {
        argument = this.requireArgsNonNull(argument);

        if (engine.getPlugin().getName().equalsIgnoreCase(argument.getPlugin().getName()))
            throw new IllegalArgumentException("Cannot disable the plugin itself.");
    }
}
