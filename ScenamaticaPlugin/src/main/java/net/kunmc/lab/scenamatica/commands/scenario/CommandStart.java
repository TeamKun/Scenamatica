package net.kunmc.lab.scenamatica.commands.scenario;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommandStart extends CommandBase
{
    private final ScenamaticaRegistry registry;

    @SneakyThrows(TriggerNotFoundException.class)
    @Override
    public void onCommand(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        if (indicateArgsLengthInvalid(terminal, strings, 2))
            return;

        String pluginName = strings[0];
        String scenarioName = strings[1];

        Plugin plugin;
        if ((plugin = Bukkit.getPluginManager().getPlugin(pluginName)) == null)
        {
            terminal.error(LangProvider.get("command.scenario.start.errors.noPlugin"));
            return;
        }

        try
        {
            this.registry.getScenarioManager().queueScenario(plugin, scenarioName, TriggerType.MANUAL_DISPATCH);
            terminal.success(LangProvider.get("command.scenario.start.success"));
        }
        catch (ScenarioNotFoundException e)
        {
            terminal.error(LangProvider.get("command.scenario.start.errors.noScenario"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        if (strings.length == 1)
            return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .map(Plugin::getName)
                    .collect(Collectors.toList());
        else if (strings.length == 2)
        {
            String pluginName = strings[0];
            Plugin plugin;
            if ((plugin = Bukkit.getPluginManager().getPlugin(pluginName)) == null)
                return null;
            Map<String, ScenarioFileBean> scenarios = this.registry.getScenarioFileManager().getPluginScenarios(plugin);
            if (scenarios == null)
                return null;
            return new ArrayList<>(scenarios.keySet());
        }

        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "scenamatica.use.scenario.start";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return (TextComponent) LangProvider.getComponent("command.scenario.start.help");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "string"),
                required("scenarioName", "string"),
        };
    }
}
