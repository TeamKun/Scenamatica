package org.kunlab.scenamatica.bookkeeper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Main
{
    public static void main(String[] args)
    {
        OptionParser parser = createParser();
        OptionSet options = parser.parse(args);

        BookkeeperConfig config = parseOptions(options);

        BookkeeperCore core = new BookkeeperCore(config);
        core.start();
    }

    private static OptionParser createParser()
    {

        OptionParser parser = new OptionParser();

        parser.acceptsAll(Arrays.asList("input", "i"))
                .withRequiredArg()
                .required()
                .describedAs("Path to the target JAR file");
        parser.acceptsAll(Arrays.asList("output", "o"))
                .withRequiredArg()
                .required()
                .describedAs("Output directory path");
        parser.acceptsAll(Arrays.asList("classpath", "cp"))
                .withOptionalArg()
                .ofType(String.class).withValuesSeparatedBy(" ").describedAs("Paths to additional classes or libraries");
        parser.acceptsAll(Arrays.asList("artifact", "a"))
                .withOptionalArg()
                .defaultsTo("ledger.zip").describedAs("Name of the artifact file");
        parser.acceptsAll(Arrays.asList("compressionLevel", "cl"))
                .withOptionalArg()
                .ofType(Integer.class).defaultsTo(9).describedAs("Compression level of the artifact file");
        parser.acceptsAll(Arrays.asList("language", "l"))
                .withOptionalArg()
                .defaultsTo(Locale.JAPANESE.toString()).describedAs("Language/locale setting");
        parser.acceptsAll(Arrays.asList("resolveEvents", "e"))
                .withOptionalArg()
                .ofType(Boolean.class).defaultsTo(true).describedAs("Whether to resolve events");
        parser.acceptsAll(Arrays.asList("eventsURL", "eu"))
                .withOptionalArg()
                .defaultsTo("https://raw.githubusercontent.com/sya-ri/spigot-event-list/master/data/events.json").describedAs("URL for events data");
        parser.acceptsAll(Arrays.asList("eventsLicenseURL", "elu"))
                .withOptionalArg()
                .defaultsTo("https://raw.githubusercontent.com/sya-ri/spigot-event-list/master/LICENSE").describedAs("URL for events license");
        parser.acceptsAll(Arrays.asList("threads", "t"))
                .withOptionalArg()
                .ofType(Integer.class).defaultsTo(4).describedAs("Number of threads to use");
        parser.acceptsAll(Collections.singletonList("debug"))
                .withOptionalArg()
                .ofType(Boolean.class).defaultsTo(false).describedAs("Enable debug mode");

        return parser;
    }

    @SuppressWarnings("unchecked")
    private static BookkeeperConfig parseOptions(OptionSet options)
    {
        List<Path> classPaths = new ArrayList<>();
        if (options.has("classpath"))
        {
            for (String path : (List<String>) options.valuesOf("classpath"))
            {
                if (path.startsWith("$paper:"))
                    classPaths.add(PaperClassPathFinder.findLocalRepositoryJar(path.substring("$paper:".length())));
                else
                    classPaths.add(Paths.get(path));
            }
        }

        return BookkeeperConfig.builder()
                .targetJar(Paths.get((String) options.valueOf("input")))
                .outputDir(Paths.get((String) options.valueOf("output")))
                .artifactFileName((String) options.valueOf("artifact"))
                .artifactCompressionLevel((Integer) options.valueOf("compressionLevel"))
                .classPaths(classPaths)
                .language(Locale.forLanguageTag((String) options.valueOf("language")))
                .resolveEvents((Boolean) options.valueOf("resolveEvents"))
                .eventsURL((String) options.valueOf("eventsURL"))
                .eventsLicenseURL((String) options.valueOf("eventsLicenseURL"))
                .threads((Integer) options.valueOf("threads"))
                .debug((Boolean) options.valueOf("debug"))
                .build();
    }
}
