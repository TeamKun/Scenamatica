package net.kunmc.lab.scenamatica.context;

import lombok.Getter;
import net.kunmc.lab.scenamatica.context.utils.WorldUtils;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.StageManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.WorldBean;
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

    public StageManagerImpl(ScenamaticaRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    @NotNull
    public World createStage(WorldBean bean)
    {
        if (this.stage != null)
            return this.stage;

        String stageName = "stage_" + UUID.randomUUID().toString().substring(0, 8);
        NamespacedKey key = NamespacedKey.fromString("scenamatica:" + stageName);
        assert key != null;

        if (bean.getOriginalName() != null)
        {
            World copied = WorldUtils.copyWorld(bean.getOriginalName(), key);
            this.hasCopied = true;
            return copied;
        }

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
            throw new IllegalStateException("Failed to create a stage: " + bean.getOriginalName());

        this.stage.setAutoSave(false);

        return this.stage;
    }

    @Override
    public void destroyStage()
    {
        if (this.stage == null)
            return;

        this.stage.getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

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
