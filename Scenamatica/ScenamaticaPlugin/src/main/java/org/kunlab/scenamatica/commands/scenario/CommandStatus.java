package org.kunlab.scenamatica.commands.scenario;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
public class CommandStatus extends CommandBase
{
    private final ScenamaticaRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        ScenarioManager scenarioManager = this.registry.getScenarioManager();
        if (!scenarioManager.isRunning())
        {
            terminal.info(LangProvider.get("command.scenario.status.notRunning"));
            return;
        }

        ScenarioEngine engine = scenarioManager.getCurrentScenario();
        assert engine != null;

        terminal.info(LangProvider.get(
                "command.scenario.status.running",
                MsgArgs.of("plugin", engine.getPlugin().getName())
                        .add("scenario", engine.getScenario().getName())
        ));

        terminal.info(LangProvider.get(
                        "command.scenario.status.details.id",
                        MsgArgs.of("id", engine.getTestID().toString().substring(0, 8))
                )
        );

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String sAtStr = formatter.format(new Date(engine.getStartedAt()));

        terminal.info(LangProvider.get(
                        "command.scenario.status.details.time",
                        MsgArgs.of("startedAt", sAtStr)
                )
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Terminal terminal, String[] strings)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "scenamatica.use.scenario.status";
    }

    @Override
    public Text getHelpOneLine()
    {
        return Text.ofTranslatable("command.scenario.status.help");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
