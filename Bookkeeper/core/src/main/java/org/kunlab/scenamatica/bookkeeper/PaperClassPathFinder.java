package org.kunlab.scenamatica.bookkeeper;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PaperClassPathFinder
{
    public static final String REPO_ROOT = System.getProperty("user.home") + "/.m2/repository/";
    public static final String REPO_PAPER = REPO_ROOT + "com/destroystokyo/paper/paper-api/";

    public static final String ARTIFACT_PAPER_API = "paper-api";

    public static Path findLocalRepositoryJar(String versionPrefix)
    {
        Path repoPath = Paths.get(REPO_PAPER);
        if (!repoPath.toFile().exists())
            throw new IllegalStateException("Repository not found: " + REPO_PAPER);

        return findDirectory(repoPath, versionPrefix);
    }

    @SneakyThrows(IOException.class)
    private static Path findDirectory(Path directory, String prefix)
    {
        return Files.walk(directory)
                .filter(p -> p.getName(p.getNameCount() - 1).toString().contains(prefix))
                .filter(p -> {
                    String fileName = p.getFileName().toString();

                    return fileName.startsWith(ARTIFACT_PAPER_API) && fileName.endsWith(".jar")
                            && !(fileName.contains("-sources.") || fileName.contains("-javadoc."));
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find Paper artifact v" + prefix + " in " + directory));
    }
}
