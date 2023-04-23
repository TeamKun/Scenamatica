package net.kunmc.lab.scenamatica;

import net.kunmc.lab.peyangpaperutils.PeyangPaperUtils;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.scenamatica.commands.CommandDebug;
import net.kunmc.lab.scenamatica.commands.CommandEnable;
import net.kunmc.lab.scenamatica.commands.CommandScenario;
import net.kunmc.lab.scenamatica.events.PlayerJoinEventListener;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.reporter.BukkitTestReporter;
import net.kunmc.lab.scenamatica.reporter.RawTestReporter;
import net.kunmc.lab.scenamatica.settings.ActorSettingsImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Scenamatica extends JavaPlugin
{
    private ScenamaticaRegistry registry;
    @SuppressWarnings("FieldCanBeLocal")  // 参照を維持する必要がある。
    private CommandManager commandManager;

    public Scenamatica()
    {
    }

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        this.getConfig();
        PeyangPaperUtils.init(this);

        this.registry = new ScenamaticaDaemon(Environment.builder(this)
                .exceptionHandler(new SimpleExceptionHandler(this.getLogger()))
                .testReporter(this.getTestReporter())
                .actorSettings(ActorSettingsImpl.fromConfig(this.getConfig()))
                .build()
        );


        if (!this.initLangProvider())
            return;

        this.registry.getScenarioManager().setEnabled(
                this.getConfig().getBoolean("scenario.enabled", true)
        );
        this.registry.init();

        this.initCommands();
        this.initTestRecipient();
    }

    private TestReporter getTestReporter()
    {
        boolean isRaw = this.getConfig().getBoolean("interfaces.raw", false);
        if (isRaw)
            return new RawTestReporter();
        else
            return new BukkitTestReporter();
    }

    private void initTestRecipient()
    {
        PlayerJoinEventListener listener = new PlayerJoinEventListener(this.registry);
        this.getServer().getPluginManager().registerEvents(listener, this);

        Bukkit.getOnlinePlayers().forEach(player ->
        {
            PlayerJoinEvent event = new PlayerJoinEvent(player, Component.empty());
            listener.onPlayerJoin(event);
        });
    }

    private boolean initLangProvider()
    {
        try
        {
            LangProvider.init(this);
            LangProvider.setLanguage(this.getConfig().getString("interfaces.lang", "ja_JP"));
            return true;
        }
        catch (IOException e)
        {
            this.getLogger().warning("Failed to load language file.");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return false;
        }

    }

    private void initCommands()
    {
        this.commandManager = new CommandManager(
                this,
                "scenamatica",
                "Scenamatica",
                "scenamatica.use"
        );

        this.commandManager.registerCommand("debug", new CommandDebug(this.registry));
        this.commandManager.registerCommand("enable", new CommandEnable(this.registry));
        this.commandManager.registerCommand("scenario", new CommandScenario(this.registry));


    }

    @Override
    public void onDisable()
    {
        this.registry.shutdown();
    }
}
