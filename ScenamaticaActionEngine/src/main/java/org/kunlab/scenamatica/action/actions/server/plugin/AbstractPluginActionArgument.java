package org.kunlab.scenamatica.action.actions.server.plugin;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractPluginActionArgument extends AbstractActionArgument
{
    public static final String KEY_PLUGIN = "plugin";

    private final String plugin;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractPluginActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractPluginActionArgument a = (AbstractPluginActionArgument) argument;
        return Objects.equals(this.plugin, a.plugin);
    }

    @NotNull
    public Plugin getPlugin()
    {
        Plugin plugin;
        if ((plugin = Bukkit.getPluginManager().getPlugin(this.plugin)) == null)
            throw new IllegalArgumentException("Plugin " + this.plugin + " is not found.");

        return plugin;
    }

    public String getPluginName()
    {
        return this.plugin;
    }

    public boolean isPluginSet()
    {
        return this.plugin != null;
    }

    @Override
    public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
    {
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            ensurePresent(KEY_PLUGIN, this.plugin);
    }
}
