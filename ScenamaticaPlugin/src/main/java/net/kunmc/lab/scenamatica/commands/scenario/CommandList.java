package net.kunmc.lab.scenamatica.commands.scenario;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.utils.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        Map<String, ScenarioFileBean> scenarioMap = this.registry.getScenarioFileManager().getPluginScenarios(plugin);
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

        scenarioMap.values().stream().parallel()
                .sorted(Comparator.comparing(ScenarioFileBean::getName))
                .forEachOrdered(scenario -> this.printScenario(terminal, plugin, scenario));
    }

    private void printScenario(Terminal terminal, Plugin plugin, ScenarioFileBean scenario)
    {
        boolean canManualDispatch = scenario.getTriggers().stream().parallel()
                .anyMatch(trigger -> trigger.getType() == TriggerType.MANUAL_DISPATCH);
        String manualDispatch = LangProvider.get(canManualDispatch ? "command.enable.enable": "command.enable.disable");

        TextComponent entry = Component.text(LangProvider.get(
                "command.scenario.list.entry",
                MsgArgs.of("scenario", scenario.getName())
                        .add("description", scenario.getDescription())
                        .add("manually", manualDispatch)
        ));

        if (canManualDispatch)
            entry = entry.hoverEvent(HoverEvent.showText(LangProvider.getComponent(
                            "command.scenario.list.suggest",
                            MsgArgs.of("scenario", scenario.getName())
                    )))
                    .clickEvent(ClickEvent.suggestCommand(
                            "/scenamatica scenario start " + plugin.getName() + " " + scenario.getName()
                    ));

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
    public TextComponent getHelpOneLine()
    {
        return of(LangProvider.get("command.scenario.list.help"));
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "string")
        };
    }
}
