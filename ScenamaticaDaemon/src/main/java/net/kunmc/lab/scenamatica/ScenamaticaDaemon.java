package net.kunmc.lab.scenamatica;

import lombok.Getter;
import net.kunmc.lab.scenamatica.context.player.PlayerMockManagerImpl;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaEnvironment;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.interfaces.PlayerMockManager;
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
    private final PlayerMockManager playerMockManager;

    public ScenamaticaDaemon(ScenamaticaEnvironment env)
    {
        this.logger = env.getLogger();
        this.environment = env;
        this.exceptionHandler = env.getExceptionHandler();
        this.scenarioFileManager = new ScenarioFileManagerImpl(this);
        this.playerMockManager = new PlayerMockManagerImpl(this);
    }

    @Override
    public void shutdown()
    {
        this.playerMockManager.shutdown();
    }
}
