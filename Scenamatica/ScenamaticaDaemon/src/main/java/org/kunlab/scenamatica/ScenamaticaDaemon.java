package org.kunlab.scenamatica;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.kunlab.scenamatica.action.ActionManagerImpl;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.context.ContextManagerImpl;
import org.kunlab.scenamatica.events.PluginEventListener;
import org.kunlab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.interfaces.ScenamaticaEnvironment;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionManager;
import org.kunlab.scenamatica.interfaces.context.ContextManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioManager;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import org.kunlab.scenamatica.interfaces.trigger.TriggerManager;
import org.kunlab.scenamatica.scenario.ScenarioManagerImpl;
import org.kunlab.scenamatica.scenariofile.ScenarioFileManagerImpl;
import org.kunlab.scenamatica.trigger.TriggerManagerImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class ScenamaticaDaemon implements ScenamaticaRegistry
{
    private final Logger logger;
    private final Plugin plugin;
    private final TestReporter testReporter;
    private final ScenamaticaEnvironment environment;
    private final ExceptionHandler exceptionHandler;
    private final ScenarioFileManager scenarioFileManager;
    private final ContextManager contextManager;
    private final ActionManager actionManager;
    private final TriggerManager triggerManager;
    private final ScenarioManager scenarioManager;

    public ScenamaticaDaemon(ScenamaticaEnvironment env)
    {
        this.logger = env.getLogger();
        this.plugin = env.getPlugin();
        this.testReporter = env.getTestReporter();
        this.exceptionHandler = env.getExceptionHandler();
        this.environment = env;
        this.scenarioFileManager = new ScenarioFileManagerImpl(this);
        this.actionManager = new ActionManagerImpl(this);
        this.triggerManager = new TriggerManagerImpl(this);
        this.scenarioManager = new ScenarioManagerImpl(this);

        try
        {
            this.contextManager = new ContextManagerImpl(this);
        }
        catch (VersionNotSupportedException e)
        {
            this.logger.log(Level.SEVERE, "Bukkit " + e.getVersion() + " is not supported yet.");
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void init()
    {
        ThreadingUtil.init(this);
        this.actionManager.init();
        this.scenarioManager.init();

        Bukkit.getPluginManager().registerEvents(new PluginEventListener(this), this.plugin);
    }

    @Override
    public void shutdown()
    {
        this.contextManager.shutdown();
        this.actionManager.shutdown();
        this.scenarioManager.shutdown();

        Bukkit.getScheduler().getActiveWorkers().forEach((worker) ->
        {
            if (worker.getOwner().equals(this.plugin))
                worker.getThread().interrupt();
        });
    }
}
