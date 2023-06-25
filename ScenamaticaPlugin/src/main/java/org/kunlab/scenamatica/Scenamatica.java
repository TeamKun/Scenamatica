package org.kunlab.scenamatica;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import org.kunlab.scenamatica.commands.CommandDebug;
import org.kunlab.scenamatica.commands.CommandEnable;
import org.kunlab.scenamatica.commands.CommandScenario;
import org.kunlab.scenamatica.events.PlayerJoinEventListener;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.reporter.BukkitTestReporter;
import org.kunlab.scenamatica.reporter.CompactBukkitTestReporter;
import org.kunlab.scenamatica.reporter.JUnitReporter;
import org.kunlab.scenamatica.reporter.RawTestReporter;
import org.kunlab.scenamatica.reporter.ReportersBridge;
import org.kunlab.scenamatica.results.ScenarioResultWriter;
import org.kunlab.scenamatica.settings.ActorSettingsImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Scenamatica extends JavaPlugin
{
    private ScenarioResultWriter resultWriter;
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

        boolean isRaw = this.getConfig().getBoolean("reporting.raw", false);
        boolean isVerbose = this.getConfig().getBoolean("reporting.verbose", true);
        boolean isJunitReportingEnabled = this.getConfig().getBoolean("reporting.junit.enabled", true);

        ExceptionHandler exceptionHandler = new SimpleExceptionHandler(this.getLogger(), isRaw);
        this.initJUnitReporter(exceptionHandler);

        this.registry = new ScenamaticaDaemon(Environment.builder(this)
                .exceptionHandler(exceptionHandler)
                .testReporter(this.getTestReporter(isRaw, isVerbose, isJunitReportingEnabled))
                .actorSettings(ActorSettingsImpl.fromConfig(this.getConfig()))
                .verbose(isVerbose)
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

    private void initJUnitReporter(ExceptionHandler exceptionHandler)
    {
        FileConfiguration config = this.getConfig();
        Path directory = this.getDataFolder().toPath()
                .resolve(config.getString("reporting.junit.directory", "reports"));
        String fileNamePattern = config.getString("reporting.junit.filePattern", "yyyy-MM-dd-HH-mm-ss.xml");

        this.resultWriter = new ScenarioResultWriter(directory, exceptionHandler, fileNamePattern);
        this.resultWriter.init(this);
    }

    private TestReporter getTestReporter(boolean isRaw, boolean isVerbose, boolean isJunitReportingEnabled)
    {
        List<TestReporter> reporters = new ArrayList<>();

        if (isRaw)
            reporters.add(new RawTestReporter());

        if (isVerbose)
            reporters.add(new BukkitTestReporter());
        else
            reporters.add(new CompactBukkitTestReporter());

        if (isJunitReportingEnabled)
            reporters.add(new JUnitReporter(this.resultWriter));

        return new ReportersBridge(reporters);
    }

    private void initTestRecipient()
    {
        if (!(this.registry.getTestReporter() instanceof BukkitTestReporter))
            return;

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

        this.commandManager.registerCommand("enable", new CommandEnable(this.registry));
        this.commandManager.registerCommand("scenario", new CommandScenario(this.registry));

        if (Constants.DEBUG_BUILD)
            this.commandManager.registerCommand("debug", new CommandDebug(this.registry));
    }

    @Override
    public void onDisable()
    {
        this.registry.shutdown();
    }
}
