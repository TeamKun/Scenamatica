package org.kunlab.scenamatica.commands.debug;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.util.List;

public class CommandDestroyStage extends CommandBase
{
    private final ScenamaticaRegistry registry;

    public CommandDestroyStage(ScenamaticaRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        try
        {
            this.registry.getContextManager().getStageManager().destroyStage();
        }
        catch (StageNotCreatedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Destroy the stage");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
