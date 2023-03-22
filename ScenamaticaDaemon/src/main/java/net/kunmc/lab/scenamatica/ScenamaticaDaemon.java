package net.kunmc.lab.scenamatica;

import lombok.Getter;
import net.kunmc.lab.scenamatica.context.actor.ActorManagerImpl;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaEnvironment;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.interfaces.ActorMockManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import net.kunmc.lab.scenamatica.scenariofile.ScenarioFileManagerImpl;

import java.util.logging.Logger;

@Getter
public class ScenamaticaDaemon implements ScenamaticaRegistry
{
    private final Logger logger;
    private final ScenamaticaEnvironment environment;
    private final ExceptionHandler exceptionHandler;
    private final ScenarioFileManager scenarioFileManager;
    private final ActorMockManager actorManager;

    public ScenamaticaDaemon(ScenamaticaEnvironment env)
    {
        this.logger = env.getLogger();
        this.environment = env;
        this.exceptionHandler = env.getExceptionHandler();
        this.scenarioFileManager = new ScenarioFileManagerImpl(this);
        this.actorManager = new ActorManagerImpl(this);
    }

    @Override
    public void shutdown()
    {
        this.actorManager.shutdown();
    }
}
