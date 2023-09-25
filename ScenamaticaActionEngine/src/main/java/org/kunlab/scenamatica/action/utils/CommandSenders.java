package org.kunlab.scenamatica.action.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSenders
{
    public static final String CONSOLE_SENDER = "<CONSOLE>";

    public static boolean isSpecifiedSender(CommandSender actualSender, String expectedSender)
    {
        if (expectedSender == null)
            return true;

        if (!(actualSender instanceof Player) && expectedSender.equalsIgnoreCase(CONSOLE_SENDER))
            return true;

        return actualSender.getName().equalsIgnoreCase(expectedSender);
    }

    public static CommandSender getCommandSenderOrThrow(String specifier)
    {
        if (specifier == null)
            throw new IllegalArgumentException("specifier is null");

        CommandSender sender = getCommandSenderOrNull(specifier);
        if (sender == null)
            throw new IllegalArgumentException("specifier is invalid");
        else
            return sender;
    }

    public static CommandSender getCommandSenderOrNull(String specifier)
    {
        if (specifier == null)
            return null;

        if (specifier.equalsIgnoreCase(CONSOLE_SENDER))
            return Bukkit.getConsoleSender();

        return PlayerUtils.getPlayerOrNull(specifier);
    }

    public static CommandSender resolveSenderOrConsoleOrThrow(String specifier)
    {
        if (specifier == null)
            return Bukkit.getConsoleSender();

        return getCommandSenderOrThrow(specifier);
    }
}
