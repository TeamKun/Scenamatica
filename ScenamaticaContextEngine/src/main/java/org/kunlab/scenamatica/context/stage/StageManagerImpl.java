package org.kunlab.scenamatica.context.stage;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.context.utils.WorldUtils;
import org.kunlab.scenamatica.enums.StageType;
import org.kunlab.scenamatica.exceptions.context.stage.StageAlreadyDestroyedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Stage;
import org.kunlab.scenamatica.interfaces.context.StageManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageStructure;
import org.kunlab.scenamatica.nms.NMSProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class StageManagerImpl implements StageManager
{
    private final ScenamaticaRegistry registry;
    private final List<Stage> stages;

    private boolean destroyed;

    public StageManagerImpl(ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.stages = new ArrayList<>();
    }

    @NotNull
    private static NamespacedKey generateStageKey()
    {
        String stageName = "stage_" + UUID.randomUUID().toString().substring(0, 8);
        NamespacedKey key = NamespacedKey.fromString("scenamatica:" + stageName);
        assert key != null;
        return key;
    }

    @Override
    @NotNull
    public Stage createStage(@NotNull StageStructure structure) throws StageCreateFailedException
    {
        NamespacedKey key = generateStageKey();

        if (structure.getOriginalWorldName() != null)
            return this.createStage(structure.getOriginalWorldName());

        WorldCreator creator = new WorldCreator(key);
        if (structure.getEnvironment() != null)
            creator.environment(structure.getEnvironment());
        else
            creator.environment(World.Environment.NORMAL);
        if (structure.getSeed() != null)
            creator.seed(structure.getSeed());
        creator.type(structure.getType());
        creator.generateStructures(structure.isGenerateStructures());
        creator.hardcore(structure.isHardcore());

        World world = creator.createWorld();
        if (world == null)
            throw new StageCreateFailedException(key.toString());

        world.setAutoSave(false);

        return this.registerStage(world, StageType.GENERATED);
    }

    @Override
    public @NotNull Stage createStage(String originalName) throws StageCreateFailedException
    {
        World copied;
        try
        {
            copied = WorldUtils.copyWorld(originalName, generateStageKey());
        }
        catch (IllegalArgumentException e)
        {
            throw new StageCreateFailedException(originalName, e);
        }

        return this.registerStage(copied, StageType.CLONE);
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
        this.deleteDirectory(worldPath);
        NMSProvider.getProvider().wrap(Bukkit.getServer())
                .getWorlds().removeWorld(stageWorld.getKey());

        this.stages.remove(stage);
    }

    @Override
    public void shutdown()
    {
        if (this.destroyed)
            return;

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

    private void deleteDirectory(@NotNull Path path)
    {
        try (Stream<Path> walker = Files.walk(path))
        {
            walker
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);
        }
        catch (IOException e)
        {
            this.registry.getExceptionHandler().report(e);
        }
    }
}
