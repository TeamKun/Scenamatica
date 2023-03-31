package net.kunmc.lab.scenamatica.commands;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kunmc.lab.scenamatica.commands.scenario.CommandCancel;
import net.kunmc.lab.scenamatica.commands.scenario.CommandStart;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandScenario extends SubCommandWith
{
    private final Map<String, CommandBase> commands;

    public CommandScenario(ScenamaticaRegistry registry)
    {
        this.commands = new HashMap<>();

        this.commands.put("start", new CommandStart(registry));
        this.commands.put("cancel", new CommandCancel(registry));
    }

    @Override
    protected String getName()
    {
        return "scenario";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender commandSender)
    {
        return this.commands;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "scenamatica.use.scenario";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return (TextComponent) LangProvider.getComponent("command.scenario.help");
    }
}
