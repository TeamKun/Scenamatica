package org.kunlab.scenamatica.context.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public class WorldUtils
{
    @NotNull
    public static World copyWorld(@NotNull String originalName, @NotNull NamespacedKey key)
    {
        World original = Bukkit.getWorld(originalName);
        if (original == null)
            throw new IllegalArgumentException("World " + originalName + " does not exist.");

        Path dest = Bukkit.getWorldContainer().toPath().resolve(key.getKey());
        copyWorldFiles(original.getWorldFolder().toPath(), dest);

        WorldCreator creator = new WorldCreator(key);
        creator.copy(original);

        return Objects.requireNonNull(creator.createWorld());
    }

    private static void copyWorldFiles(Path source, Path dest)
    {
        try (Stream<Path> stream = Files.walk(source))
        {
            stream.forEach(path ->
            {
                String fileName = path.getFileName().toString();
                if (fileName.equalsIgnoreCase("uid.dat") || fileName.equalsIgnoreCase("session.lock"))
                    return;

                Path destPath = dest.resolve(source.relativize(path));
                try
                {
                    Files.copy(path, destPath);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to copy world files.", e);
        }
    }
}
