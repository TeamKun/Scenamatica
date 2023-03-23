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
import org.bukkit.entity.Player;
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

    private boolean isWorldPrepared;
    private boolean isActorPrepared;

    public ContextManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actorManager = new ActorManagerImpl(registry);
        this.stageManager = new StageManagerImpl(registry);

        this.isWorldPrepared = false;
        this.isActorPrepared = false;
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
            if (context.getWorld().getOriginalName() != null
                    && Bukkit.getWorld(context.getWorld().getOriginalName()) != null)  // 既存だったら再利用する。
            {
                logger.log(Level.INFO, "[TEST-{}] Found the stage with named {}.", context.getWorld().getOriginalName());
                logger.log(Level.INFO, "[TEST-{}] Cloning the stage...", scenarioName);
            }

            logger.log(Level.INFO, "[TEST-{}] Creating stage...", scenarioName);
            stage = this.stageManager.createStage(context.getWorld());
        }
        else
            stage = Bukkit.getWorlds().get(0);  // 通常ワールドを取得する。

        this.isWorldPrepared = true;

        if (context.getActors() != null && !context.getActors().isEmpty())
        {
            logger.log(Level.INFO, "[TEST-{}] Generating actors...", scenarioName);
            try
            {
                for (PlayerBean actor : context.getActors())
                {
                    this.actorManager.createActor(stage, actor);
                    this.isActorPrepared = true;
                }
            }
            catch (Exception e)
            {
                this.registry.getExceptionHandler().report(e);
                logger.log(Level.SEVERE, "[TEST-{}] Failed to generate actor.", e);

                this.stageManager.destroyStage();
                return false;
            }
        }

        logger.log(Level.INFO, "[TEST-{}] Context has been prepared.", scenarioName);
        return true;
    }

    @Override
    public void destroyContext()
    {
        if (!this.isWorldPrepared)
            this.stageManager.destroyStage();

        if (this.isActorPrepared)
            for (Player actor : this.actorManager.getActors())
                this.actorManager.destroyActor(actor);

    }

    @Override
    public void shutdown()
    {
        this.actorManager.shutdown();
        if (this.stageManager.isStageCreated())
            this.stageManager.destroyStage();
    }
}
