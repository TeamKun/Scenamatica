package net.kunmc.lab.scenamatica;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.scenamatica.commands.CommandDebug;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Scenamatica extends JavaPlugin implements Listener
{
    private final ScenamaticaRegistry registry;

    @SuppressWarnings("FieldCanBeLocal")  // 参照を維持する必要がある。
    private CommandManager commandManager;

    public Scenamatica()
    {
        this.registry = new ScenamaticaDaemon(Environment.builder()
                .logger(this.getLogger())
                .exceptionHandler(new SimpleExceptionHandler(this.getLogger()))
                .build()
        );
    }

    @Override
    public void onEnable()
    {
        this.commandManager = new CommandManager(
                this,
                "scenamatica",
                "Scenamatica",
                "scenamatica.use"
        );

        this.commandManager.registerCommand("debug", new CommandDebug(this.registry));

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
        this.registry.shutdown();
    }
}