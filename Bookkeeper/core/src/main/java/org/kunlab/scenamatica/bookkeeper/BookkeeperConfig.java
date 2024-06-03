package org.kunlab.scenamatica.bookkeeper;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

@Value
@Builder
public class BookkeeperConfig
{
    @NotNull
    Path targetJar;
    @NotNull
    @Singular
    List<Path> classPaths;
    @NotNull
    Path outputDir;

    @Builder.Default
    int threads = 4;
}
