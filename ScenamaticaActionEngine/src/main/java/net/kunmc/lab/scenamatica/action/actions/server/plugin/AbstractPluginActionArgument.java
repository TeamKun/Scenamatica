package net.kunmc.lab.scenamatica.action.actions.server.plugin;

import lombok.AllArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractPluginActionArgument implements ActionArgument
{
    public static final String KEY_PLUGIN = "plugin";

    @Nullable
    private final String plugin;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractPluginActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractPluginActionArgument a = (AbstractPluginActionArgument) argument;
        return this.isSamePlugin(a);
    }

    protected boolean isSamePlugin(AbstractPluginActionArgument argument)
    {
        return Objects.equals(this.plugin, argument.plugin);
    }

    @NotNull
    public Plugin getPlugin(@NotNull ScenarioEngine engine)
    {
        Plugin plugin;
        if (this.plugin == null)
            plugin = engine.getPlugin();
        else if ((plugin = Bukkit.getPluginManager().getPlugin(this.plugin)) == null)
            throw new IllegalArgumentException("Plugin " + this.plugin + " is not found.");

        return plugin;
    }

    public String getPluginName()
    {
        return this.plugin;
    }
}
