package net.kunmc.lab.scenamatica.commands.scenario;

import lombok.*;
import net.kunmc.lab.peyangpaperutils.lang.*;
import net.kunmc.lab.peyangpaperutils.lib.command.*;
import net.kunmc.lab.peyangpaperutils.lib.terminal.*;
import net.kunmc.lab.scenamatica.enums.*;
import net.kunmc.lab.scenamatica.exceptions.scenario.*;
import net.kunmc.lab.scenamatica.interfaces.*;
import net.kunmc.lab.scenamatica.interfaces.scenario.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.*;
import net.kyori.adventure.text.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

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
            queueAll(plugin, terminal, scenarioNames);
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
            // プラグインのシナリオで, 手動実行できるシナリオを持つプラグインをフィルタ。
            return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .filter(plugin -> {
                        Map<String, ScenarioFileBean> scenarios = this.registry.getScenarioFileManager().getPluginScenarios(plugin);
                        return scenarios != null && scenarios.values().stream().parallel()
                                .anyMatch(scenario -> scenario.getTriggers().stream().parallel()
                                        .anyMatch(trigger -> trigger.getType() == TriggerType.MANUAL_DISPATCH));
                    })
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
