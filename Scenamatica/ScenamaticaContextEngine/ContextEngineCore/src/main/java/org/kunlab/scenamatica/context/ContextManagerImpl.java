package org.kunlab.scenamatica.context;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.context.actor.ActorManagerImpl;
import org.kunlab.scenamatica.context.stage.StageManagerImpl;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.exceptions.context.EntityCreationException;
import org.kunlab.scenamatica.exceptions.context.actor.ActorCreationException;
import org.kunlab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageAlreadyDestroyedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.context.ContextManager;
import org.kunlab.scenamatica.interfaces.context.Stage;
import org.kunlab.scenamatica.interfaces.context.StageManager;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.structures.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.world.NMSPersistentEntitySectionManager;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;
import org.spigotmc.SpigotConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private final Map<UUID, CompletableFuture<Context>> creatingContext;

    public ContextManagerImpl(@NotNull ScenamaticaRegistry registry) throws VersionNotSupportedException
    {
        this.registry = registry;
        this.verbose = registry.getEnvironment().isVerbose();
        this.actorManager = new ActorManagerImpl(registry);
        this.stageManager = new StageManagerImpl(registry);
        this.logger = registry.getLogger();
        this.chunkLoader = new EntityChunkLoader(registry);
        this.creatingContext = new HashMap<>();

        // ゴミを残さないように
        disableSpigotFeatures();
    }

    private static void disableSpigotFeatures()
    {
        try
        {
            SpigotConfig.disablePlayerDataSaving = true;
            SpigotConfig.userCacheCap = 0;
        }
        catch (NoSuchFieldError ignored)
        {

        }
    }

    private static void enableSpigotFeatures()
    {
        try
        {
            SpigotConfig.disablePlayerDataSaving = false;
            SpigotConfig.userCacheCap = 1000;
        }
        catch (NoSuchFieldError ignored)
        {

        }
    }

    private static MsgArgs getArgs(ScenarioFileStructure scenario, UUID testID)
    {
        return MsgArgs.of("prefix", LogUtils.gerScenarioPrefix(testID, scenario));
    }

    private Stage prepareStage(ContextStructure context, ScenarioFileStructure scenario, UUID testID) throws StageCreateFailedException
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

        long timeoutMillis = 59 * 1000;
        int maxAttemptCounts = 3;

        return this.stageManager.createStage(context.getWorld(), timeoutMillis, maxAttemptCounts);
    }

    @Nullable
    private List<Actor> tryPrepareActors(World defaultWorld, ContextStructure context)
    {
        List<Actor> actors = new ArrayList<>();
        try
        {
            for (PlayerStructure actor : context.getActors())
                actors.add(this.actorManager.createActor(defaultWorld, actor));

            return actors;
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);

            for (Actor actor : actors)
                this.actorManager.destroyActor(actor);

            return null;
        }

    }

    @SuppressWarnings("unchecked")
    private <T extends Entity> T spawnEntity(@NotNull World stage, @NotNull EntityStructure entity) throws EntityCreationException
    {
        EntityType type = entity.getType();
        if (type == null)
            throw new EntityCreationException("Unable to spawn entity: type is null");

        Location spawnLoc;
        World spawnWorld;
        if (entity.getLocation() == null)
        {
            final int DEFAULT_LOC_X = 0;
            final int DEFAULT_LOC_Z = 0;

            int y = stage.getHighestBlockYAt(DEFAULT_LOC_X, DEFAULT_LOC_Z);

            spawnWorld = stage;  // 指定がない場合は, ステージにスポーン
            spawnLoc = new Location(stage, DEFAULT_LOC_X, y, DEFAULT_LOC_Z);
        }
        else
        {
            // {指定がない ? ステージ : 指定されたワールド} にスポーン
            LocationStructure entityLoc = entity.getLocation();
            World world;
            if (entityLoc.getWorld() == null)
                world = stage;  // ステージに作成する
            else
                world = null;  // create() メソッド内部で自動的に指定される。

            spawnLoc = entity.getLocation().create(world);
            spawnWorld = spawnLoc.getWorld();
        }

        UUID entityTag = UUID.randomUUID();
        String tagName = "scenamatica-" + entityTag;
        Entity generatedEntity = spawnWorld.spawnEntity(spawnLoc, type);
        entity.applyTo(generatedEntity);
        generatedEntity.addScoreboardTag(tagName);
        this.chunkLoader.addEntity(generatedEntity);

        // 1.17 以上なら, エンティティのトラッキングとチック経過を開始する
        if (MinecraftVersion.current().isInRange(MinecraftVersion.V1_17, MinecraftVersion.V1_17_1))
        {
            NMSProvider.tryDoNMS(() -> {
                NMSWorldServer nmsWorld = NMSProvider.getProvider().wrap(spawnWorld);
                NMSPersistentEntitySectionManager<Entity> nmsEntityManager = nmsWorld.getEntityManager();

                nmsEntityManager.startTracking(generatedEntity);
                nmsEntityManager.startTicking(generatedEntity);
            });
        }

        return (T) generatedEntity;
    }

    @Override
    public Context prepareContext(@NotNull ScenarioFileStructure scenario, @NotNull UUID testID)
            throws StageAlreadyDestroyedException, StageCreateFailedException, ActorCreationException, EntityCreationException
    {
        if (this.creatingContext.containsKey(testID))
            throw new IllegalStateException("Context is already being created.");

        CompletableFuture<Context> future = CompletableFuture.supplyAsync(() -> {
            try
            {
                return this.prepareContext0(scenario, testID);
            }
            catch (StageCreateFailedException | StageAlreadyDestroyedException | ActorCreationException |
                   EntityCreationException e)
            {
                throw new CompletionException(e);
            }
        }).whenComplete((context, throwable) -> {
            if (throwable != null)
            {
                this.registry.getExceptionHandler().report(throwable);
                if (context != null)
                    this.destroyContext(context);
            }
        });

        this.creatingContext.put(testID, future);

        try
        {
            return future.join();
        }
        catch (CompletionException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof StageCreateFailedException)
                throw (StageCreateFailedException) cause;
            if (cause instanceof StageAlreadyDestroyedException)
                throw (StageAlreadyDestroyedException) cause;
            if (cause instanceof ActorCreationException)
                throw (ActorCreationException) cause;
            if (cause instanceof EntityCreationException)
                throw (EntityCreationException) cause;

            throw new IllegalStateException("Unknown exception", e);
        }
    }

    private Context prepareContext0(ScenarioFileStructure scenario, UUID testID) throws StageAlreadyDestroyedException,
            StageCreateFailedException, ActorCreationException, EntityCreationException
    {
        this.logIfVerbose(scenario, "context.creating", testID);
        ContextStructure context = scenario.getContext();

        this.logIfVerbose(scenario, "context.stage.generating", testID);
        Stage stage = this.prepareStage(context, scenario, testID);

        List<Actor> actors;
        if (!(context == null || context.getActors().isEmpty()))
        {
            this.logIfVerbose(scenario, "context.actor.generating", testID);
            actors = this.tryPrepareActors(stage.getWorld(), context);
            if (actors == null)
            {
                this.logActorGenFail(scenario, testID);
                // 失敗したのでロールバック
                stage.destroy();
                throw new ActorCreationException();
            }
        }
        else
            actors = Collections.emptyList();

        List<Entity> generatedEntities;
        if (!(context == null || context.getEntities().isEmpty()))
        {
            this.logIfVerbose(scenario, "context.entity.generating", testID);
            generatedEntities = this.tryPrepareEntities(stage, context);
            if (generatedEntities == null) // エラー発生
            {
                // 失敗したのでロールバック
                stage.destroy();
                actors.forEach(this.actorManager::destroyActor);
                throw new EntityCreationException();
            }
        }
        else
            generatedEntities = Collections.emptyList();


        this.logIfVerbose(scenario, "context.created", testID);
        return new ContextImpl(
                this,
                stage,
                Collections.unmodifiableList(actors),
                Collections.unmodifiableList(generatedEntities)
        );
    }

    private List<Entity> tryPrepareEntities(Stage stage, ContextStructure context)
    {
        List<Entity> entities = new ArrayList<>();
        try
        {
            // Asynchronous chunk load! を避けるために同期処理
            ThreadingUtil.waitForOrThrow(() -> {
                for (EntityStructure entity : context.getEntities())
                    entities.add(this.spawnEntity(stage.getWorld(), entity));

                return null;
            });

            return entities;
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);

            return null;
        }
    }

    @Override
    public void cancelCreation(@NotNull UUID testID)
    {
        CompletableFuture<Context> future = this.creatingContext.remove(testID);
        if (future != null)
            try
            {
                future.cancel(true);
            }
            catch (CancellationException ignored)
            {
            }
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

    @Override
    @SneakyThrows(StageAlreadyDestroyedException.class)
    public void destroyContext(Context context)
    {
        if (context.hasActors())
        {
            List<Actor> actors = new ArrayList<>(context.getActors());  // ConcurrentModificationException 対策
            for (Actor actor : actors)
                this.actorManager.destroyActor(actor);
        }

        if (context.hasEntities())
        {
            for (Entity entity : context.getEntities())
                if (entity != null)
                {
                    this.chunkLoader.removeEntity(entity);
                    entity.remove();
                }
        }

        if (context.hasStage())
            this.stageManager.destroyStage(context.getStage());  // StageNotCreatedException はチェック済み。
    }

    @Override
    public void shutdown()
    {
        this.actorManager.shutdown();
        this.stageManager.shutdown();
        this.chunkLoader.shutdown();

        enableSpigotFeatures();
    }
}
