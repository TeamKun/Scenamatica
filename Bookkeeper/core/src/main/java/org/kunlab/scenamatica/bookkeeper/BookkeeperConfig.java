package org.kunlab.scenamatica.bookkeeper;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

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
    @NotNull
    @Builder.Default
    String artifactFileName = "ledger.zip";
    @Builder.Default
    int artifactCompressionLevel = 9;

    @Builder.Default
    Locale language = Locale.JAPANESE;

    @Builder.Default
    boolean resolveEvents = true;
    @Builder.Default
    String eventsURL = "https://raw.githubusercontent.com/sya-ri/spigot-event-list/master/data/events.json";
    @Builder.Default
    String eventsLicenseURL = "https://raw.githubusercontent.com/sya-ri/spigot-event-list/master/LICENSE";

    @Builder.Default
    int threads = 4;

    @Builder.Default
    boolean generateJsonSchema = false;
    @Builder.Default
    String jsonSchemaFileName = "json-schema.json";

    @Builder.Default
    boolean debug = false;
}
