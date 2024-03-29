package org.kunlab.scenamatica.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class CommandEnable extends CommandBase
{
    private final ScenamaticaRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (args.length == 0)
        {
            terminal.info(this.getCurrentCause());
            return;
        }

        String stateStr = args[0];
        if (!stateStr.equalsIgnoreCase("true") && !stateStr.equalsIgnoreCase("false"))
        {
            terminal.error(LangProvider.get("command.enable.invalidState"));
            return;
        }

        boolean state = Boolean.parseBoolean(stateStr);

        this.registry.getScenarioManager().setEnabled(state);
        terminal.success(this.getCurrentCause());
    }

    private String getCurrentCause()
    {
        return LangProvider.get("command.enable.current", MsgArgs.of(
                "state", this.registry.getScenarioManager().isEnabled() ? "%%command.enable.enable%%": "%%command.enable.disable%%"
        ));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return Arrays.asList("true", "false");
    }

    @Override
    public @Nullable String getPermission()
    {
        return "scenamatica.use.enable";
    }

    @Override
    public Text getHelpOneLine()
    {
        return Text.ofTranslatable("command.enable.help");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("state", "boolean")
        };
    }
}
