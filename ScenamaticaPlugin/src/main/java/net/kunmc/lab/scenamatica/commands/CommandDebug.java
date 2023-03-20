package net.kunmc.lab.scenamatica.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandDebug extends SubCommandWith
{
    private static final Map<String, CommandBase> COMMANDS;

    static
    {
        COMMANDS = new HashMap<>();
    }

    @Override
    protected String getName()
    {
        return "scenario";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender sender)
    {
        return COMMANDS;
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
