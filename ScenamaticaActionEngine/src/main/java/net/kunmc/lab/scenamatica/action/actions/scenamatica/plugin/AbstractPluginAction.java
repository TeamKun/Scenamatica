package net.kunmc.lab.scenamatica.action.actions.scenamatica.plugin;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractPluginAction<A extends AbstractPluginActionArgument> extends AbstractAction<A>
{
    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof PluginEvent))
            return false;

        PluginEvent e = (PluginEvent) event;

        return Objects.equals(e.getPlugin().getName(), argument.getPlugin(engine).getName());
    }

    protected String deserializePlugin(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractPluginActionArgument.KEY_PLUGIN);

        return map.get(AbstractPluginActionArgument.KEY_PLUGIN).toString();
    }

}
