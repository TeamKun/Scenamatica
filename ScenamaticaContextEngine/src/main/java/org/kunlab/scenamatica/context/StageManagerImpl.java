package org.kunlab.scenamatica.context;

import lombok.Getter;
import org.kunlab.scenamatica.context.utils.WorldUtils;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.StageManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageBean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

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

    @Override
    @NotNull
    public World createStage(StageBean bean) throws StageCreateFailedException
    {
        if (this.stage != null)
            return this.stage;

        NamespacedKey key = generateStageKey();

        if (bean.getOriginalWorldName() != null)
            return this.createStage(bean.getOriginalWorldName());

        WorldCreator creator = new WorldCreator(key);
        if (bean.getEnvironment() != null)
            creator.environment(bean.getEnvironment());
        else
            creator.environment(World.Environment.NORMAL);
        if (bean.getSeed() != null)
            creator.seed(bean.getSeed());
        creator.type(bean.getType());
        creator.generateStructures(bean.isGenerateStructures());
        creator.hardcore(bean.isHardcore());

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
    public void destroyStage() throws StageNotCreatedException
    {
        if (this.stage == null)
            throw new StageNotCreatedException();
        else if (this.isShared)  // 共有ステージの場合は壊すと大変なことになる。
        {
            this.stage = null;
            return;
        }

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.getLocation().getWorld().equals(this.stage))
                .forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        Bukkit.unloadWorld(this.stage, false);

        Path worldPath = this.stage.getWorldFolder().toPath();
        this.deleteDirectory(worldPath);

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
