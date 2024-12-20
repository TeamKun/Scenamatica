package org.kunlab.scenamatica.commands.debug;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.structures.minecraft.entity.PlayerStructureImpl;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CommandSummonActor extends CommandBase
{
    private final ScenamaticaRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String name = args[0];
        Runner.runAsync(() -> this.registry.getContextManager().getActorManager().createActor(Bukkit.getWorlds().get(0), new PlayerStructureImpl(
                name,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                null,
                null,
                4,
                new ArrayList<>()
        )));
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
    public Text getHelpOneLine()
    {
        return of("Summon actor for debug");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("name", "string")
        };
    }
}
