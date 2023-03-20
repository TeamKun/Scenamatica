package net.kunmc.lab.scenamatica;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.scenamatica.commands.CommandDebug;
import org.bukkit.plugin.java.JavaPlugin;

public final class ScenamaticaPlugin extends JavaPlugin
{
    private CommandManager commandManager;

    @Override
    public void onEnable()
    {
        this.commandManager = new CommandManager(
                this,
                "scenamatica",
                "Scenamatica",
                "scenamatica.use"
        );

        this.commandManager.registerCommand("debug", new CommandDebug());
    }

    @Override
    public void onDisable() {

    }
}
