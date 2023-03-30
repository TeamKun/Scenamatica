package net.kunmc.lab.scenamatica;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.scenamatica.commands.CommandDebug;
import net.kunmc.lab.scenamatica.commands.CommandEnable;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Scenamatica extends JavaPlugin implements Listener
{
    private final ScenamaticaRegistry registry;

    @SuppressWarnings("FieldCanBeLocal")  // 参照を維持する必要がある。
    private CommandManager commandManager;

    public Scenamatica()
    {
        this.registry = new ScenamaticaDaemon(Environment.builder(this)
                .exceptionHandler(new SimpleExceptionHandler(this.getLogger()))
                .testReporter(new TestReportRecipient())
                .build()
        );

    }

    @Override
    public void onEnable()
    {
        try
        {
            LangProvider.init(this);
        }
        catch (IOException e)
        {
            this.getLogger().warning("Failed to load language file.");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.commandManager = new CommandManager(
                this,
                "scenamatica",
                "Scenamatica",
                "scenamatica.use"
        );

        this.commandManager.registerCommand("debug", new CommandDebug(this.registry));
        this.commandManager.registerCommand("enable", new CommandEnable(this.registry));

        this.getServer().getPluginManager().registerEvents(this, this);

        this.registry.getScenarioManager().setEnabled(
                this.getConfig().getBoolean("scenario.enabled", true)
        );
        this.registry.init();
    }

    @Override
    public void onDisable()
    {
        this.registry.shutdown();
    }
}
