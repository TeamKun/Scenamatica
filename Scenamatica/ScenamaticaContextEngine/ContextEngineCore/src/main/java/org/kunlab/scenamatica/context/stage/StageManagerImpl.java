package org.kunlab.scenamatica.context.stage;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.context.utils.WorldUtils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.StageType;
import org.kunlab.scenamatica.exceptions.context.stage.StageAlreadyDestroyedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Stage;
import org.kunlab.scenamatica.interfaces.context.StageManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class StageManagerImpl implements StageManager
{
    private final ScenamaticaRegistry registry;
    private final List<Stage> stages;
    private List<Path> garbageDirectories;

    private boolean destroyed;

    public StageManagerImpl(ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.stages = new ArrayList<>();
        this.garbageDirectories = new ArrayList<>();

        new GarbageCleaner3000TM().runTaskTimer(
                registry.getPlugin(),
                0,
                20 * 5
        );
    }

    private static WorldCreator cretateWorldCreator(StageStructure structure)
    {
        WorldCreator creator = new WorldCreator("stage_" + UUID.randomUUID().toString().substring(0, 8));
        if (structure.getEnvironment() != null)
            creator.environment(structure.getEnvironment());
        else
            creator.environment(World.Environment.NORMAL);
        if (structure.getSeed() != null)
            creator.seed(structure.getSeed());
        creator.type(structure.getType());
        creator.generateStructures(structure.isGenerateStructures());

        return creator;
    }

    private World generateWorld(WorldCreator creator, long timeoutMillis)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<? extends World> future = executor.submit(() -> ThreadingUtil.waitForOrThrow(this.registry, creator::createWorld));

        try
        {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException | InterruptedException e)
        {
            future.cancel(true);
            return null;
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            executor.shutdown();
        }
    }

    @Override
    public @NotNull Stage createStage(String originalName) throws StageCreateFailedException
    {
        World copied;
        try
        {
            copied = WorldUtils.copyWorld(originalName, "stage_" + UUID.randomUUID().toString().substring(0, 8));
        }
        catch (IllegalArgumentException e)
        {
            throw new StageCreateFailedException(originalName, e);
        }

        return this.registerStage(copied, StageType.CLONE);
    }

    @Override
    @NotNull
    public Stage createStage(@NotNull StageStructure structure, long timeoutMillis, int maxAttemptCounts) throws StageCreateFailedException
    {
        if (structure.getOriginalWorldName() != null)
            return ThreadingUtil.waitForOrThrow(this.registry, () -> this.createStage(structure.getOriginalWorldName()));

        WorldCreator creator = cretateWorldCreator(structure);
        World world = ThreadingUtil.waitFor(this.registry, creator::createWorld);
       /* for (int i = 0; i < maxAttemptCounts; i++)
        {
            world = this.generateWorld(creator, timeoutMillis);
            if (world != null)
                break;
        }*/

        // hardCore だけはここで設定する必要がある。
        if (structure.isHardcore())
        {
            NMSWorldServer nmsWorldServer = NMSProvider.getProvider().wrap(world);
            nmsWorldServer.getWorldData().setHardcore(true);
        }

        if (world == null)
            throw new StageCreateFailedException(creator.name());

        world.setAutoSave(false);

        return this.registerStage(world, StageType.GENERATED);
    }

    private Stage registerStage(World world, StageType type)
    {
        Stage stage = new StageImpl(world, type, this);
        this.stages.add(stage);
        return stage;
    }

    @Override
    public @NotNull Stage shared(@NotNull String name)
    {
        World stage;
        if ((stage = Bukkit.getWorld(name)) == null)
            throw new IllegalArgumentException("World " + name + " is not loaded.");

        return this.registerStage(stage, StageType.SHARED);
    }

    @Override
    public void destroyStage(@NotNull Stage stage) throws StageAlreadyDestroyedException
    {
        if (stage.isDestroyed())
            throw new StageAlreadyDestroyedException();
        else if (stage.getType() == StageType.SHARED)
            return;  // SHARED ステージは破棄しない

        World stageWorld = stage.getWorld();

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.getLocation().getWorld().equals(stageWorld))
                .forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        Bukkit.unloadWorld(stageWorld, false);

        Path worldPath = stageWorld.getWorldFolder().toPath();
        this.garbageDirectories.add(worldPath);

        this.stages.remove(stage);
    }

    @Override
    public void shutdown()
    {
        if (this.destroyed)
            return;

        new GarbageCleaner3000TM().run();

        this.destroyed = true;

        new ArrayList<>(this.stages)
                .forEach(stage ->
                {
                    try
                    {
                        this.destroyStage(stage);
                    }
                    catch (StageAlreadyDestroyedException e)
                    {
                        this.registry.getExceptionHandler().report(e);
                    }
                });
    }

    private class GarbageCleaner3000TM extends BukkitRunnable
    {
        @Override
        public void run()
        {
            if (StageManagerImpl.this.destroyed)
            {
                this.cancel();
                return;
            }
            else if (StageManagerImpl.this.garbageDirectories.isEmpty())
                return;

            List<Path> garbageDirectories = new ArrayList<>(StageManagerImpl.this.garbageDirectories);
            for (Path path : garbageDirectories)
            {
                tryDeleteOneWorld(path);
                if (!Files.exists(path))
                    StageManagerImpl.this.garbageDirectories.remove(path);
            }
        }

        private void tryDeleteOneWorld(Path path)
        {
            try(Stream<Path> paths = Files.walk(path))
            {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            catch (IOException e)
            {
                StageManagerImpl.this.registry.getExceptionHandler().report(e);
            }
        }
    }
}
