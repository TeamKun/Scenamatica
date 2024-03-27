package org.kunlab.scenamatica.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commands.scenario.CommandCancel;
import org.kunlab.scenamatica.commands.scenario.CommandList;
import org.kunlab.scenamatica.commands.scenario.CommandStart;
import org.kunlab.scenamatica.commands.scenario.CommandStatus;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

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
        this.commands.put("status", new CommandStatus(registry));
        this.commands.put("list", new CommandList(registry));
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
    public Text getHelpOneLine()
    {
        return Text.ofTranslatable("command.scenario.help");
    }
}
