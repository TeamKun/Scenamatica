package org.kunlab.scenamatica.commands.debug;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.scenariofile.beans.context.StageBeanImpl;

import java.util.List;

public class CommandCreateStage extends CommandBase
{
    private final ScenamaticaRegistry registry;

    public CommandCreateStage(ScenamaticaRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String name = args[0];

        try
        {
            this.registry.getContextManager().getStageManager().createStage(new StageBeanImpl(
                    name,
                    WorldType.NORMAL,
                    null,
                    false,
                    null,
                    false
            ));
        }
        catch (StageCreateFailedException e)
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
        return of("Create a stage");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("name", "string"),
        };
    }
}
