package org.kunlab.scenamatica.commands.scenario;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.SessionCreator;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.kunlab.scenamatica.utils.CommandUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CommandStart extends CommandBase
{
    private final ScenamaticaRegistry registry;

    @Override
    @SneakyThrows({ScenarioNotFoundException.class, TriggerNotFoundException.class})
    public void onCommand(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        if (indicateArgsLengthInvalid(terminal, strings, 1))
            return;

        String pluginName = strings[0];

        Plugin plugin;
        if ((plugin = Bukkit.getPluginManager().getPlugin(pluginName)) == null)
        {
            terminal.error(LangProvider.get(
                    "command.scenario.start.errors.noPlugin",
                    MsgArgs.of("plugin", pluginName)
            ));
            return;
        }

        if (strings.length < 2)
        {
            List<ScenarioEngine> engines = this.registry.getScenarioManager().getEnginesFor(plugin);

            SessionCreator creator = this.registry.getScenarioManager().newSession();
            for (ScenarioEngine engine : engines)
                creator.add(engine, TriggerType.MANUAL_DISPATCH);

            this.registry.getScenarioManager().queueScenario(creator);
        }
        else
        {
            String[] scenarioNames = Arrays.copyOfRange(strings, 1, strings.length);
            this.queueAll(plugin, terminal, scenarioNames);
        }
    }

    private void queueAll(Plugin plugin, Terminal terminal, String[] scenarioNames)
    {
        SessionCreator creator = this.registry.getScenarioManager().newSession();
        for (String scenarioName : scenarioNames)
            creator.add(plugin, TriggerType.MANUAL_DISPATCH, scenarioName);

        try
        {
            this.registry.getScenarioManager().queueScenario(creator);

            terminal.success(LangProvider.get("command.scenario.start.success"));
        }
        catch (ScenarioNotFoundException e)
        {
            terminal.error(LangProvider.get(
                    "command.scenario.start.errors.noScenario",
                    MsgArgs.of("scenario", e.getScenarioName())
            ));
        }
        catch (TriggerNotFoundException e)
        {
            terminal.error(LangProvider.get(
                    "command.scenario.start.errors.noManually",
                    MsgArgs.of("scenario", e.getScenarioName())
            ));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        if (strings.length == 1)
            // プラグインのシナリオで, 手動実行できるシナリオを持つプラグインを選択。
            return CommandUtils.getScenariosByTrigger(this.registry, TriggerType.MANUAL_DISPATCH);
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
