package net.kunmc.lab.scenamatica.context;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.scenamatica.commons.utils.LogUtils;
import net.kunmc.lab.scenamatica.commons.utils.ThreadingUtil;
import net.kunmc.lab.scenamatica.context.actor.ActorManagerImpl;
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import net.kunmc.lab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import net.kunmc.lab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.context.StageManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;

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
    private final Logger logger;

    private boolean isWorldPrepared;
    private boolean isActorPrepared;

    public ContextManagerImpl(@NotNull ScenamaticaRegistry registry) throws VersionNotSupportedException
    {
        this.registry = registry;
        this.actorManager = new ActorManagerImpl(registry, this);
        this.stageManager = new StageManagerImpl(registry);
        this.logger = registry.getLogger();

        this.isWorldPrepared = false;
        this.isActorPrepared = false;

        // ゴミを残さないように
        SpigotConfig.disablePlayerDataSaving = true;
        SpigotConfig.userCacheCap = 0;
    }

    @Override
    public Context prepareContext(@NotNull ScenarioFileBean scenario, @NotNull UUID testID)
            throws StageNotCreatedException
    {
        ContextBean context = scenario.getContext();

        this.log(scenario, "context.creating", testID);

        this.log(scenario, "context.stage.generating", testID);

        World stage;
        if (context != null && context.getWorld() != null)
        {
            if (context.getWorld().getOriginalWorldName() != null
                    && Bukkit.getWorld(context.getWorld().getOriginalWorldName()) != null)  // 既存だったら再利用する。
            {
                this.log(scenario, "context.stage.clone.found",
                        MsgArgs.of("stageName", context.getWorld().getOriginalWorldName()), testID
                );
                this.log(scenario, "context.stage.clone.cloning",
                        MsgArgs.of("stageName", context.getWorld().getOriginalWorldName()), testID
                );
            }

            stage = ThreadingUtil.waitFor(this.registry.getPlugin(), () ->
            {
                try
                {
                    return this.stageManager.createStage(context.getWorld());
                }
                catch (StageCreateFailedException e)
                {
                    throw new RuntimeException(e);
                }
            });
        }
        else
            stage = this.stageManager.shared(DEFAULT_ORIGINAL_WORLD_NAME);

        this.isWorldPrepared = true;

        List<Actor> actors = new ArrayList<>();
        if (context != null && !context.getActors().isEmpty())
        {
            this.log(scenario, "context.actor.generating", testID);
            try
            {
                this.isActorPrepared = ThreadingUtil.waitFor(this.registry.getPlugin(), () -> {
                    try
                    {
                        for (PlayerBean actor : context.getActors())
                            actors.add(this.actorManager.createActor(actor));
                        return true;
                    }
                    catch (ContextPreparationException e)
                    {
                        throw new IllegalStateException(e);
                    }
                });
            }
            catch (Exception e)
            {
                this.registry.getExceptionHandler().report(e);
                this.logActorGenFail(scenario, testID);

                this.stageManager.destroyStage();
                return null;
            }
        }

        this.log(scenario, "context.created", testID);
        return new ContextImpl(stage, actors);
    }

    private void log(ScenarioFileBean scenario, String message, MsgArgs args, UUID testID)
    {
        this.logger.log(Level.INFO, LangProvider.get(message, getArgs(scenario, testID).add(args)));
    }

    private void log(ScenarioFileBean scenario, String message, UUID testID)
    {
        this.logger.log(Level.INFO, LangProvider.get(message, getArgs(scenario, testID)));
    }

    private void logActorGenFail(ScenarioFileBean scenario, UUID testID)
    {
        this.logger.log(Level.WARNING, LangProvider.get("context.actor.failed", getArgs(scenario, testID)));
    }

    private static MsgArgs getArgs(ScenarioFileBean scenario, UUID testID)
    {
        return MsgArgs.of("prefix", LogUtils.gerScenarioPrefix(testID, scenario));
    }

    @SneakyThrows(StageNotCreatedException.class)
    @Override
    public void destroyContext()
    {
        if (!this.isWorldPrepared)
            this.stageManager.destroyStage();  // StageNotCreatedException はチェック済み。

        if (this.isActorPrepared)
        {
            List<Actor> actors = new ArrayList<>(this.actorManager.getActors());  // ConcurrentModificationException 対策
            for (Actor actor : actors)
                this.actorManager.destroyActor(actor);
        }
    }

    @SneakyThrows(StageNotCreatedException.class)
    @Override
    public void shutdown()
    {
        this.actorManager.shutdown();
        if (this.stageManager.isStageCreated())
            this.stageManager.destroyStage();  // StageNotCreatedException はチェック済み。


        SpigotConfig.disablePlayerDataSaving = false;
        SpigotConfig.userCacheCap = 1000;  // デフォルト
    }
}
