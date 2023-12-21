package org.kunlab.scenamatica.action.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

public class PlayerLikeCommandSenders
{
    public static final String CONSOLE_SENDER = "<CONSOLE>";

    public static boolean isSpecifiedSender(@NotNull CommandSender actualSender, @NotNull PlayerSpecifier expectedSender)
    {
        if (!expectedSender.canProvideTarget())
            return true;

        // 構造体を持たない場合, コンソールだと仮定する。
        if (!expectedSender.hasStructure())
        {
            String specifier = expectedSender.getSelectorString();
            assert specifier != null;
            if (actualSender instanceof ConsoleCommandSender && specifier.equalsIgnoreCase(CONSOLE_SENDER))
                return true;
        }

        if (!(actualSender instanceof Player))
            return false;

        Player player = (Player) actualSender;

        return expectedSender.checkMatchedPlayer(player);
    }

    public static CommandSender getCommandSenderOrThrow(@NotNull PlayerSpecifier specifier, @NotNull Context context)
    {
        CommandSender sender = getCommandSenderOrNull(specifier, context);
        if (sender == null)
            throw new IllegalArgumentException("Could not select target for " + specifier);
        else
            return sender;
    }

    public static CommandSender getCommandSenderOrNull(@NotNull PlayerSpecifier specifier, @NotNull Context context)
    {
        if (!specifier.canProvideTarget())
            return null;

        if (!specifier.hasStructure())
        {
            String specifierString = specifier.getSelectorString();
            assert specifierString != null;
            if (specifierString.equalsIgnoreCase(CONSOLE_SENDER))
                return Bukkit.getConsoleSender();
        }

        return specifier.selectTarget(context).orElse(null);
    }

    public static CommandSender resolveSenderOrConsoleOrThrow(@NotNull PlayerSpecifier specifier, @NotNull Context context)
    {
        if (!specifier.canProvideTarget())
            return Bukkit.getConsoleSender();

        return getCommandSenderOrThrow(specifier, context);
    }
}
