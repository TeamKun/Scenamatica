package org.kunlab.scenamatica.context.stage;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.utils.ReflectionUtils;
import org.kunlab.scenamatica.context.utils.WorldUtils;
import org.kunlab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.StageManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

public class StageManagerImpl implements StageManager
{
    private final ScenamaticaRegistry registry;

    @Getter
    private World stage;
    private boolean hasCopied;
    private boolean isShared;

    public StageManagerImpl(ScenamaticaRegistry registry)
    {
        this.registry = registry;
    }

    @NotNull
    private static NamespacedKey generateStageKey()
    {
        String stageName = "stage_" + UUID.randomUUID().toString().substring(0, 8);
        NamespacedKey key = NamespacedKey.fromString("scenamatica:" + stageName);
        assert key != null;
        return key;
    }

    private static MinecraftServerController getMocker()
            throws VersionNotSupportedException
    {
        String version = ReflectionUtils.PackageType.getServerVersion();
        //noinspection SwitchStatementWithTooFewBranches
        switch (version)  // TODO: Support other versions.
        {
            case "v1_16_R3":
                return new org.kunlab.scenamatica.context.stage.nms.v_1_16_5.MinecraftServerControllerImpl();
            default:
                throw new VersionNotSupportedException(version);
        }
    }

    @Override
    @NotNull
    public World createStage(StageStructure structure) throws StageCreateFailedException
    {
        if (this.stage != null)
            return this.stage;

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

        this.stage = creator.createWorld();
        if (this.stage == null)
            throw new StageCreateFailedException(key.toString());

        this.stage.setAutoSave(false);

        return this.stage;
    }

    @Override
    public @NotNull World createStage(String originalName) throws StageCreateFailedException
    {
        World copied;
        try
        {
            copied = this.stage = WorldUtils.copyWorld(originalName, generateStageKey());
        }
        catch (IllegalArgumentException e)
        {
            throw new StageCreateFailedException(originalName, e);
        }
        this.hasCopied = true;
        return copied;
    }

    @Override
    public @NotNull World shared(@NotNull String name)
    {
        World stage;
        if ((stage = Bukkit.getWorld(name)) == null)
            throw new IllegalArgumentException("World " + name + " is not loaded.");

        this.isShared = true;
        return this.stage = stage;
    }

    @Override
    @SneakyThrows(VersionNotSupportedException.class)
    public void destroyStage() throws StageNotCreatedException
    {
        if (this.stage == null)
            throw new StageNotCreatedException();
        else if (this.isShared)  // 共有ステージの場合は壊すと大変なことになる。
        {
            this.stage = null;
            this.isShared = false;
            return;
        }

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.getLocation().getWorld().equals(this.stage))
                .forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        Bukkit.unloadWorld(this.stage, false);

        Path worldPath = this.stage.getWorldFolder().toPath();
        this.deleteDirectory(worldPath);
        getMocker().removeWorld(this.stage.getKey());

        this.stage = null;
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

    @Override
    public boolean isStageCreated()
    {
        return this.stage != null;
    }

    public boolean hasCopied()
    {
        return this.hasCopied;
    }
}
