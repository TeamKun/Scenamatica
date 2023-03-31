package net.kunmc.lab.scenamatica;

import lombok.Getter;
import net.kunmc.lab.scenamatica.action.ActionManagerImpl;
import net.kunmc.lab.scenamatica.context.ContextManagerImpl;
import net.kunmc.lab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaEnvironment;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import net.kunmc.lab.scenamatica.interfaces.trigger.TriggerManager;
import net.kunmc.lab.scenamatica.scenario.ScenarioManagerImpl;
import net.kunmc.lab.scenamatica.scenariofile.ScenarioFileManagerImpl;
import net.kunmc.lab.scenamatica.trigger.TriggerManagerImpl;
import org.bukkit.plugin.Plugin;

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
        this.actionManager.init();
        this.scenarioManager.init();
    }

    @Override
    public void shutdown()
    {
        this.contextManager.shutdown();
        this.actionManager.shutdown();
        this.scenarioManager.shutdown();

    }
}
