package net.kunmc.lab.scenamatica.context;

import lombok.Getter;
import net.kunmc.lab.scenamatica.context.actor.ActorManagerImpl;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.context.StageManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ContextManagerImpl implements ContextManager
{
    private final ScenamaticaRegistry registry;
    @Getter
    @NotNull
    private final ActorManager actorManager;
    @Getter
    @NotNull
    private final StageManager stageManager;

    public ContextManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actorManager = new ActorManagerImpl(registry);
        this.stageManager = new StageManagerImpl();
    }

    @Override
    public boolean prepareContext(ScenarioFileBean scenario)
    {
        String scenarioName = scenario.getName();
        ContextBean context = scenario.getContext();

        Logger logger = this.registry.getLogger();
        logger.log(Level.INFO, "[TEST-{}] Preparing context for scenario: {}", scenarioName);

        World stage;
        if (context.getWorld() != null)
        {
            logger.log(Level.INFO, "[TEST-{}] Creating stage...", scenarioName);
            stage = this.stageManager.createStage(context.getWorld());
        }
        else
            stage = Bukkit.getWorlds().get(0);  // 通常ワールドを取得する。

        if (context.getActors() != null && !context.getActors().isEmpty())
        {
            logger.log(Level.INFO, "[TEST-{}] Generating actors...", scenarioName);
            try
            {
                for (PlayerBean actor : context.getActors())
                    this.actorManager.mock(stage, actor);
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "[TEST-{}] Failed to generate actor.", e);
                return false;
            }
        }

        logger.log(Level.INFO, "[TEST-{}] Context has been prepared.", scenarioName);
        return true;
    }

    @Override
    public void shutdown()
    {
        this.actorManager.shutdown();
        if (this.stageManager.isStageCreated())
            this.stageManager.destroyStage();
    }
}
