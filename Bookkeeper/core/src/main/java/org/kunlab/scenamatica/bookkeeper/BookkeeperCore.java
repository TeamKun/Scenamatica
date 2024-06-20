package org.kunlab.scenamatica.bookkeeper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kunlab.scenamatica.bookkeeper.compiler.Compiler;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.kunlab.scenamatica.bookkeeper.reader.ActionCategoryDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.ActionDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.IAnnotationReader;
import org.kunlab.scenamatica.bookkeeper.reader.InputDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.OutputDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.OutputsDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.TypeDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.TypePropertyDefinitionReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Slf4j(topic = "Bookkeeper/Core")
public class BookkeeperCore
{
    private final BookkeeperConfig config;
    private final ScenamaticaClassLoader classLoader;
    private final AnnotationClassifier classifier;
    private final List<IAnnotationReader<? extends IDefinition>> processors;
    private final Compiler compiler;

    private final Path tempDir;

    public BookkeeperCore(BookkeeperConfig config)
    {
        log.info("Initializing Bookkeeper Core...");
        createOutputDir(config.getOutputDir());
        this.tempDir = createTempDir(config.getOutputDir());

        this.config = config;
        this.processors = new ArrayList<>();
        this.classLoader = ScenamaticaClassLoader.create(config.getTargetJar(), this.processors);
        this.classifier = new AnnotationClassifier(this.processors);
        this.compiler = new Compiler(this);

        this.classLoader.addClasspaths(this.config.getClassPaths());
        addProcessors(this.processors);

        this.compiler.init();

        log.info("Bookkeeper Core initialized.");
    }

    public void start()
    {
        log.info("Starting Bookkeeping...");

        log.info("Scanning classes...");
        this.classLoader.scanAll(this.config.getThreads());

        log.info("Looking up for annotations...");
        List<AnnotationClassifier.ClassifiedAnnotation> classifiedAnnotations =
                this.classifier.classify(this.classLoader.getScenamaticaClasses());

        log.info("Rendering classes...");
        List<IDefinition> definitions = classifiedAnnotations.stream().parallel()
                .map(AnnotationClassifier.ClassifiedAnnotation::getAnnotations)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        this.compiler.compile(definitions);

        log.info("Flushing compilers...");
        this.compiler.flush();

        log.info("Bookkeeping finished.");
    }

    @SuppressWarnings({"UnreachableCode", "ConstantValue"})
    private static Path createTempDir(Path baseDir)
    {
        boolean debug = true;

        try
        {
            Path tempDir;
            if (debug)
            {
                tempDir = baseDir.resolve("temp");
                Files.createDirectories(tempDir);
            }
            else
            {
                tempDir = Files.createTempDirectory(baseDir, "temp");
                tempDir.toFile().deleteOnExit();
            }

            return tempDir;
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to create temporary directory", e);
        }
    }

    private static void createOutputDir(Path outputDir)
    {
        try
        {
            if (Files.exists(outputDir))
                wipeDir(outputDir);

            Files.createDirectories(outputDir);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to create output directory: " + outputDir, e);
        }
    }

    private static void wipeDir(Path outputDir) throws IOException
    {
        Files.walk(outputDir)
                .sorted(Comparator.reverseOrder()) // reverse order to delete children first
                .map(Path::toFile)
                .forEach(File::delete);

        Files.deleteIfExists(outputDir);
        if (Files.exists(outputDir))
            throw new IOException("Failed to delete output directory: " + outputDir);
    }

    private static void addProcessors(List<? super IAnnotationReader<? extends IDefinition>> registry)
    {
        OutputDefinitionReader outputDefinitionReader = new OutputDefinitionReader();
        InputDefinitionReader inputDefinitionReader = new InputDefinitionReader();
        ActionDefinitionReader actionDefinitionReader = new ActionDefinitionReader(inputDefinitionReader, outputDefinitionReader);
        TypePropertyDefinitionReader propertyDefinitionReader = new TypePropertyDefinitionReader();
        TypeDefinitionReader typeDefinitionReader = new TypeDefinitionReader(propertyDefinitionReader);
        ActionCategoryDefinitionReader actionCategoryDefinitionReader = new ActionCategoryDefinitionReader();
        OutputsDefinitionReader outputsDefinitionReader = new OutputsDefinitionReader(outputDefinitionReader);

        registry.add(outputDefinitionReader);
        registry.add(inputDefinitionReader);
        registry.add(actionDefinitionReader);
        registry.add(propertyDefinitionReader);
        registry.add(typeDefinitionReader);
        registry.add(actionCategoryDefinitionReader);
        registry.add(outputsDefinitionReader);
    }

    public static void main(String[] args)
    {
        BookkeeperConfig config = BookkeeperConfig.builder()
                .targetJar(Paths.get("D:\\projects\\kun\\scenamatica\\Scenamatica\\ScenamaticaPlugin\\target\\Scenamatica-1.4.1.jar"))
                .outputDir(Paths.get("dist"))
                .classPath(PaperClassPathFinder.findLocalRepositoryJar("1.16.5"))
                .resolveEvents(false)
                .build();
        BookkeeperCore core = new BookkeeperCore(config);
        core.start();
    }
}
