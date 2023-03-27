package net.kunmc.lab.scenamatica;

import lombok.Getter;
import net.kunmc.lab.scenamatica.action.ActionManagerImpl;
import net.kunmc.lab.scenamatica.context.ContextManagerImpl;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaEnvironment;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import net.kunmc.lab.scenamatica.interfaces.trigger.TriggerManager;
import net.kunmc.lab.scenamatica.scenariofile.ScenarioFileManagerImpl;
import net.kunmc.lab.scenamatica.trigger.TriggerManagerImpl;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

@Getter
public class ScenamaticaDaemon implements ScenamaticaRegistry
{
    private final Logger logger;
    private final Plugin plugin;
    private final ScenamaticaEnvironment environment;
    private final ExceptionHandler exceptionHandler;
    private final ScenarioFileManager scenarioFileManager;
    private final ContextManager contextManager;
    private final ActionManager actionManager;
    private final TriggerManager triggerManager;

    public ScenamaticaDaemon(ScenamaticaEnvironment env)
    {
        this.logger = env.getLogger();
        this.plugin = env.getPlugin();
        this.environment = env;
        this.exceptionHandler = env.getExceptionHandler();
        this.scenarioFileManager = new ScenarioFileManagerImpl(this);
        this.contextManager = new ContextManagerImpl(this);
        this.actionManager = new ActionManagerImpl(this);
        this.triggerManager = new TriggerManagerImpl(this);
    }

    public void init()
    {
        this.actionManager.init();

    }

    @Override
    public void shutdown()
    {
        this.contextManager.shutdown();
    }
}
