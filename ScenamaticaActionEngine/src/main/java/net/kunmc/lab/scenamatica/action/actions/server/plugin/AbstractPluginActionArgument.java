package net.kunmc.lab.scenamatica.action.actions.server.plugin;

import lombok.AllArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class AbstractPluginActionArgument implements ActionArgument
{
    public static final String KEY_PLUGIN = "plugin";

    @NotNull
    private final String plugin;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractPluginActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractPluginActionArgument a = (AbstractPluginActionArgument) argument;
        return this.plugin.equalsIgnoreCase(a.plugin);
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
}
