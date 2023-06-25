package org.kunlab.scenamatica.commands.scenario;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioNotRunningException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.List;

@AllArgsConstructor
public class CommandCancel extends CommandBase
{
    private final ScenamaticaRegistry registry;

    @Override
    @SneakyThrows(ScenarioNotRunningException.class)
    public void onCommand(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        ScenarioEngine current = this.registry.getScenarioManager().getCurrentScenario();
        if (current == null)
        {
            terminal.error(LangProvider.get("command.scenario.cancel.errors.notRunning"));
            return;
        }

        terminal.info(LangProvider.get(
                "command.scenario.cancel.running",
                MsgArgs.of("scenario", current.getScenario().getName())
                        .add("plugin", current.getPlugin())
        ));

        this.registry.getScenarioManager().cancel();
        terminal.info(LangProvider.get("command.scenario.cancel.success", MsgArgs.of("scenario", current.getScenario().getName())));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "scenamatica.use.scenario.cancel";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return (TextComponent) LangProvider.getComponent("command.scenario.cancel.help");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
