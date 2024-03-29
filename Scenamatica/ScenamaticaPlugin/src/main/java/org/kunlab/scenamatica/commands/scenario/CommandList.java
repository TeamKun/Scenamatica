package org.kunlab.scenamatica.commands.scenario;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.utils.CommandUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CommandList extends CommandBase
{
    private final ScenamaticaRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        if (indicateArgsLengthInvalid(terminal, strings, 1))
            return;

        String pluginName = strings[0];

        Plugin plugin;
        if ((plugin = Bukkit.getPluginManager().getPlugin(pluginName)) == null)
        {
            terminal.error(LangProvider.get("command.scenario.list.noPlugin", MsgArgs.of("plugin", pluginName)));
            return;
        }

        Map<String, ScenarioFileStructure> scenarioMap = this.registry.getScenarioFileManager().getPluginScenarios(plugin);
        if (scenarioMap == null)
        {
            terminal.error(LangProvider.get("command.scenario.list.noPlugin", MsgArgs.of("plugin", pluginName)));
            return;
        }

        terminal.writeLine(LangProvider.get(
                        "command.scenario.list.header",
                        MsgArgs.of("plugin", plugin.getName())
                )
        );

        scenarioMap.values().stream()
                .sorted(Comparator.comparing(ScenarioFileStructure::getName))
                .forEachOrdered(scenario -> this.printScenario(terminal, plugin, scenario));
    }

    private void printScenario(Terminal terminal, Plugin plugin, ScenarioFileStructure scenario)
    {
        boolean canManualDispatch = scenario.getTriggers().stream()
                .anyMatch(trigger -> trigger.getType() == TriggerType.MANUAL_DISPATCH);
        String manualDispatch = LangProvider.get(canManualDispatch ? "command.enable.enable": "command.enable.disable");

        Text entry = Text.ofTranslatable(
                "command.scenario.list.entry",
                MsgArgs.of("scenario", scenario.getName())
                        .add("description", scenario.getDescription() == null ? "N/A": scenario.getDescription())
                        .add("manually", manualDispatch)
        );

        if (canManualDispatch)
            entry.hoverText(Text.ofTranslatable(
                            "command.scenario.list.suggest",
                            MsgArgs.of("scenario", scenario.getName())
                    ))
                    .suggestCommandOnClick("/scenamatica scenario start " + plugin.getName() + " " + scenario.getName());

        terminal.write(entry);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        return CommandUtils.getScenariosByTrigger(this.registry, TriggerType.MANUAL_DISPATCH);
    }

    @Override
    public @Nullable String getPermission()
    {
        return "scenamatica.use.scenario.list";
    }

    @Override
    public Text getHelpOneLine()
    {
        return Text.ofTranslatable("command.scenario.list.help");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "string")
        };
    }
}
