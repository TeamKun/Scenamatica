package net.kunmc.lab.scenamatica.context;

import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.StageManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.WorldBean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class StageManagerImpl implements StageManager
{
    private final ScenamaticaRegistry registry;

    private World world;

    public StageManagerImpl(ScenamaticaRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    @NotNull
    public World createStage(WorldBean bean)
    {
        if (this.world != null)
            return this.world;

        WorldCreator creator = new WorldCreator(bean.getName());
        if (bean.getEnvironment() != null)
            creator.environment(bean.getEnvironment());
        else
            creator.environment(World.Environment.NORMAL);
        if (bean.getSeed() != null)
            creator.seed(bean.getSeed());
        creator.type(bean.getType());
        creator.generateStructures(bean.isGenerateStructures());
        creator.hardcore(bean.isHardcore());

        this.world = creator.createWorld();
        if (this.world == null)
            throw new IllegalStateException("Failed to create a stage: " + bean.getName());

        this.world.setAutoSave(false);

        return this.world;
    }

    @Override
    public void destroyStage()
    {
        if (this.world == null)
            return;

        this.world.getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        Bukkit.unloadWorld(this.world, false);

        Path worldPath = this.world.getWorldFolder().toPath();
        this.deleteDirector(worldPath);

        this.world = null;
    }

    private void deleteDirector(@NotNull Path path)
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
    public World getStage()
    {
        return this.world;
    }

    @Override
    public boolean isStageCreated()
    {
        return this.world != null;
    }
}
