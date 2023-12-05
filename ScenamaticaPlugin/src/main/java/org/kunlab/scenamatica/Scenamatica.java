package org.kunlab.scenamatica;

import lombok.NoArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.kunlab.scenamatica.commands.CommandDebug;
import org.kunlab.scenamatica.commands.CommandEnable;
import org.kunlab.scenamatica.commands.CommandScenario;
import org.kunlab.scenamatica.enums.TriggerType;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
public final class Scenamatica extends JavaPlugin
{
    private ScenarioResultWriter resultWriter;
    private ScenamaticaRegistry registry;
    @SuppressWarnings("FieldCanBeLocal")  // 参照を維持する必要がある。
    private CommandManager commandManager;

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        this.getConfig();

        boolean isRaw = this.getConfig().getBoolean("reporting.raw", false);
        ExceptionHandler exceptionHandler = new SimpleExceptionHandler(this.getLogger(), isRaw);
        this.initJUnitReporter(exceptionHandler);
        this.registry = this.getRegistry(exceptionHandler);

        if (!this.initLangProvider())
            return;

        this.registry.getScenarioManager().setEnabled(
                this.getConfig().getBoolean("scenario.enabled", true)
        );
        this.registry.init();

        this.initCommands();
        this.initTestRecipient();
    }

    private ScenamaticaRegistry getRegistry(ExceptionHandler exceptionHandler)
    {
        boolean isVerbose = this.getConfig().getBoolean("reporting.verbose", true);
        boolean doRetry = this.getConfig().getBoolean("execution.retry", true);
        int maxAttemptCount = doRetry ? this.getConfig().getInt("execution.retry.maxAttempts", 3): 0;

        return new ScenamaticaDaemon(Environment.builder(this)
                .exceptionHandler(exceptionHandler)
                .testReporter(this.getTestReporter(this.getConfig()))
                .actorSettings(ActorSettingsImpl.fromConfig(this.getConfig()))
                .verbose(isVerbose)
                .ignoreTriggerTypes(this.getIgnoreTriggerTypes())
                .maxAttemptCount(maxAttemptCount)
                .build()
        );
    }

    private List<TriggerType> getIgnoreTriggerTypes()
    {
        String[] byConfig = this.getConfig().getStringList("ignoreTriggerTypes").toArray(new String[0]);
        String[] byJVMArg = JVMArguments.getIgnoreTriggerTypes();

        String[] merged = Arrays.stream(new String[][]{byConfig, byJVMArg})
                .flatMap(Arrays::stream)
                .distinct()  // 重複排除
                .toArray(String[]::new);

        List<TriggerType> result = new ArrayList<>();
        for (String s : merged)
        {
            try
            {
                result.add(TriggerType.valueOf(s));
            }
            catch (IllegalArgumentException e)
            {
                this.getLogger().warning("Unknown trigger type: " + s);
            }
        }

        return result;
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

    private TestReporter getTestReporter(FileConfiguration config)
    {
        boolean isRaw = config.getBoolean("reporting.raw", false);
        boolean isVerbose = config.getBoolean("reporting.verbose", true);
        boolean isJunitReportingEnabled = config.getBoolean("reporting.junit.enabled", true);

        List<TestReporter> reporters = new LinkedList<>();
        // 開始時 ＝＞ 昇順で実行される。
        // 終了時 ＝＞ 降順で実行される。

        if (isRaw)
            reporters.add(new RawTestReporter());

        if (isJunitReportingEnabled)
            reporters.add(new JUnitReporter(this.resultWriter));

        if (isVerbose)
            reporters.add(new BukkitTestReporter(config.getInt("execution.retry.maxAttempts", 3)));
        else
            reporters.add(new CompactBukkitTestReporter());

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
