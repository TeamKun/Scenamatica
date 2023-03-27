package net.kunmc.lab.scenamatica.context;

import lombok.Getter;
import net.kunmc.lab.scenamatica.context.actor.ActorManagerImpl;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.context.StageManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContextManagerImpl implements ContextManager
{
    private static final String DEFAULT_ORIGINAL_WORLD_NAME = "world";

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
        this.actorManager = new ActorManagerImpl(registry, this);
        this.stageManager = new StageManagerImpl(registry);

        this.isWorldPrepared = false;
        this.isActorPrepared = false;
    }

    @Override
    public Context prepareContext(@NotNull ScenarioFileBean scenario, @NotNull UUID testID)
    {
        String logPrefix = "TEST-" + StringUtils.substring(scenario.getName(), 0, 8) +
                "/" + testID.toString().substring(0, 8);
        ContextBean context = scenario.getContext();

        Logger logger = this.registry.getLogger();
        logger.log(Level.INFO, "[{}] Preparing context for scenario: {}", logPrefix);

        if (context.getWorld() != null && context.getWorld().getOriginalName() != null
                && Bukkit.getWorld(context.getWorld().getOriginalName()) != null)  // 既存だったら再利用する。
        {
            logger.log(Level.INFO, "[{}] Found the stage with named {}.", context.getWorld().getOriginalName());
            logger.log(Level.INFO, "[{}] Cloning the stage...", logPrefix);
        }

        logger.log(Level.INFO, "[{}] Creating stage...", logPrefix);

        World stage;
        if (context.getWorld() != null)
            stage = this.stageManager.createStage(context.getWorld());
        else
            stage = this.stageManager.createStage(DEFAULT_ORIGINAL_WORLD_NAME);  // TODO: コンフィグにする。

        this.isWorldPrepared = true;

        List<Player> actors = new ArrayList<>();
        if (context.getActors() != null && !context.getActors().isEmpty())
        {
            logger.log(Level.INFO, "[{}] Generating actors...", logPrefix);
            try
            {
                for (PlayerBean actor : context.getActors())
                {
                    actors.add(this.actorManager.createActor(actor));
                    this.isActorPrepared = true;
                }
            }
            catch (Exception e)
            {
                this.registry.getExceptionHandler().report(e);
                logger.log(Level.SEVERE, "[{}] Failed to generate actor.", e);

                this.stageManager.destroyStage();
                return null;
            }
        }

        logger.log(Level.INFO, "[{}] Context has been prepared.", logPrefix);
        return new ContextImpl(stage, actors);
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
