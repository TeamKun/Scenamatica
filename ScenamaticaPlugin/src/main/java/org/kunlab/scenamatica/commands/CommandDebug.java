package org.kunlab.scenamatica.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commands.debug.CommandCreateStage;
import org.kunlab.scenamatica.commands.debug.CommandSummonActor;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.util.HashMap;
import java.util.Map;

public class CommandDebug extends SubCommandWith
{
    private static Map<String, CommandBase> commands;

    public CommandDebug(ScenamaticaRegistry registry)
    {
        commands = new HashMap<>();
        commands.put("summonActor", new CommandSummonActor(registry));
        commands.put("createStage", new CommandCreateStage(registry));

    }

    @Override
    protected String getName()
    {
        return "scenario";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender sender)
    {
        return commands;
    }

    @Override
    public @Nullable String getPermission()
    {
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Scenamatica のデバッグコマンドです。");
    }
}
