package org.kunlab.scenamatica.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.context.actor.ActorManagerImpl;
import org.kunlab.scenamatica.context.stage.StageManagerImpl;
import org.kunlab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.context.ContextManager;
import org.kunlab.scenamatica.interfaces.context.StageManager;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.GenericEntityStructure;
import org.spigotmc.SpigotConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ContextManagerImpl implements ContextManager
{
    private static final String DEFAULT_ORIGINAL_WORLD_NAME = "world";

    private final ScenamaticaRegistry registry;
    private final boolean verbose;
    @Getter
    @NotNull
    private final ActorManager actorManager;
    @Getter
    @NotNull
    private final StageManager stageManager;
    private final EntityChunkLoader chunkLoader;
    private final Logger logger;
    @Getter(AccessLevel.PACKAGE)
    List<UUID> generatedEntities;
    private boolean isWorldPrepared;
    private boolean isActorPrepared;

    public ContextManagerImpl(@NotNull ScenamaticaRegistry registry) throws VersionNotSupportedException
    {
        this.registry = registry;
        this.verbose = registry.getEnvironment().isVerbose();
        this.actorManager = new ActorManagerImpl(registry, this);
        this.stageManager = new StageManagerImpl(registry);
        this.logger = registry.getLogger();
        this.chunkLoader = new EntityChunkLoader(registry);

        this.isWorldPrepared = false;
        this.isActorPrepared = false;

        // ゴミを残さないように
        SpigotConfig.disablePlayerDataSaving = true;
        SpigotConfig.userCacheCap = 0;
    }

    private static MsgArgs getArgs(ScenarioFileStructure scenario, UUID testID)
    {
        return MsgArgs.of("prefix", LogUtils.gerScenarioPrefix(testID, scenario));
    }

    private World prepareWorld(ContextStructure context, ScenarioFileStructure scenario, UUID testID) throws StageCreateFailedException
    {
        if (context == null || context.getWorld() == null)
            return this.stageManager.shared(DEFAULT_ORIGINAL_WORLD_NAME);

        if (context.getWorld().getOriginalWorldName() != null
                && Bukkit.getWorld(context.getWorld().getOriginalWorldName()) != null)  // 既存だったら再利用する。
        {
            this.logIfVerbose(scenario, "context.stage.clone.found",
                    MsgArgs.of("stageName", context.getWorld().getOriginalWorldName()),
                    testID
            );
            this.logIfVerbose(scenario, "context.stage.clone.cloning",
                    MsgArgs.of("stageName", context.getWorld().getOriginalWorldName()),
                    testID
            );
        }

        return ThreadingUtil.waitForOrThrow(this.registry, () ->
                this.stageManager.createStage(context.getWorld())
        );
    }

    private List<Actor> prepareActors(ContextStructure context, ScenarioFileStructure scenario, UUID testID) throws StageCreateFailedException, StageNotCreatedException
    {
        try
        {
            List<Actor> actors = new ArrayList<>();
            for (PlayerStructure actor : context.getActors())
                actors.add(this.actorManager.createActor(actor));

            this.isActorPrepared = true;

            return actors;
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            this.logActorGenFail(scenario, testID);

            this.stageManager.destroyStage();
            return null;
        }

    }

    private <T extends Entity> T spawnEntity(World stage, GenericEntityStructure entity)
    {
        EntityType type = entity.getType();
        if (type == null)
            throw new IllegalArgumentException("Unable to spawn entity: type is null");

        Location spawnLoc;
        if (entity.getLocation() == null)
        {
            final int DEFAULT_LOC_X = 0;
            final int DEFAULT_LOC_Z = 0;

            int y = stage.getHighestBlockYAt(DEFAULT_LOC_X, DEFAULT_LOC_Z);

            spawnLoc = new Location(stage, DEFAULT_LOC_X, y, DEFAULT_LOC_Z);
        }
        else
            spawnLoc = entity.getLocation();


        return ThreadingUtil.waitForOrThrow(this.registry, () -> {
                    UUID entityTag = UUID.randomUUID();
                    String tagName = "scenamatica-" + entityTag;
                    @SuppressWarnings("unchecked")  // 一見 unchecked に見えるが、spawnEntity は T を返す。
                    T e = (T) stage.spawnEntity(spawnLoc, type, CreatureSpawnEvent.SpawnReason.CUSTOM,
                            generatedEntity -> {
                                ((Mapped<T>) entity).applyTo((T) generatedEntity);
                                generatedEntity.addScoreboardTag(tagName);
                            }
                    );

                    this.chunkLoader.addEntity(e);

                    return e;
                }
        );
    }

    private List<Entity> prepareEntities(World stage, ContextStructure context, ScenarioFileStructure scenario, UUID testID) throws StageCreateFailedException, StageNotCreatedException
    {
        List<Entity> entities = new ArrayList<>();
        for (GenericEntityStructure entity : context.getEntities())
            entities.add(this.spawnEntity(stage, entity));

        this.isActorPrepared = true;

        return entities;
    }

    @Override
    public Context prepareContext(@NotNull ScenarioFileStructure scenario, @NotNull UUID testID)
            throws StageNotCreatedException, StageCreateFailedException
    {
        ContextStructure context = scenario.getContext();

        this.logIfVerbose(scenario, "context.creating", testID);

        this.logIfVerbose(scenario, "context.stage.generating", testID);
        World stage = this.prepareWorld(context, scenario, testID);
        this.isWorldPrepared = true;

        List<Actor> actors = null;
        if (!(context == null || context.getActors().isEmpty()))
        {
            this.logIfVerbose(scenario, "context.actor.generating", testID);
            actors = this.prepareActors(context, scenario, testID);
        }

        List<Entity> generatedEntities = null;
        if (!(context == null || context.getEntities().isEmpty()))
        {
            this.logIfVerbose(scenario, "context.entity.generating", testID);
            generatedEntities = this.prepareEntities(stage, context, scenario, testID);

            this.generatedEntities = generatedEntities.stream()
                    .map(Entity::getUniqueId)
                    .collect(Collectors.toList());
        }


        this.logIfVerbose(scenario, "context.created", testID);
        return new ContextImpl(stage, actors, generatedEntities);
    }

    private void logIfVerbose(ScenarioFileStructure scenario, String message, MsgArgs args, UUID testID)
    {
        if (!this.verbose)
            return;
        this.logger.log(Level.INFO, LangProvider.get(message, getArgs(scenario, testID).add(args)));
    }

    private void logIfVerbose(ScenarioFileStructure scenario, String message, UUID testID)
    {
        if (!this.verbose)
            return;
        this.logger.log(Level.INFO, LangProvider.get(message, getArgs(scenario, testID)));
    }

    private void logActorGenFail(ScenarioFileStructure scenario, UUID testID)
    {
        this.logger.log(Level.WARNING, LangProvider.get("context.actor.failed", getArgs(scenario, testID)));
    }

    @SneakyThrows(StageNotCreatedException.class)
    @Override
    public void destroyContext()
    {
        if (this.isWorldPrepared)
            this.stageManager.destroyStage();  // StageNotCreatedException はチェック済み。

        if (this.isActorPrepared)
        {
            List<Actor> actors = new ArrayList<>(this.actorManager.getActors());  // ConcurrentModificationException 対策
            for (Actor actor : actors)
                this.actorManager.destroyActor(actor);
        }

        this.chunkLoader.clear();
        if (this.generatedEntities != null)
        {
            for (UUID uuid : this.generatedEntities)
            {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null)
                    entity.remove();
            }
        }

        this.isWorldPrepared = false;
        this.isActorPrepared = false;
        this.generatedEntities = null;

    }

    @SneakyThrows(StageNotCreatedException.class)
    @Override
    public void shutdown()
    {
        this.actorManager.shutdown();
        if (this.stageManager.isStageCreated())
            this.stageManager.destroyStage();  // StageNotCreatedException はチェック済み。
        this.chunkLoader.clear();


        SpigotConfig.disablePlayerDataSaving = false;
        SpigotConfig.userCacheCap = 1000;  // デフォルト
    }
}
