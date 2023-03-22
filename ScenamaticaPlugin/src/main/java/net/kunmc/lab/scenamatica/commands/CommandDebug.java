package net.kunmc.lab.scenamatica.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kunmc.lab.scenamatica.commands.debug.CommandCreateStage;
import net.kunmc.lab.scenamatica.commands.debug.CommandSummonActor;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandDebug extends SubCommandWith
{
    private static Map<String, CommandBase> commands;

    public CommandDebug(ScenamaticaRegistry registory)
    {
        commands = new HashMap<>();
        commands.put("summonActor", new CommandSummonActor(registory));
        commands.put("createStage", new CommandCreateStage(registory));

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
