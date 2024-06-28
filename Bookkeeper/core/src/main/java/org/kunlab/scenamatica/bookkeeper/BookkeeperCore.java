package org.kunlab.scenamatica.bookkeeper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kunlab.scenamatica.bookkeeper.compiler.CategoryManager;
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
import java.io.InputStream;
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
    private static final Path LICENSE_FILE = Paths.get("LICENSE");
    private static final Path README_FILE = Paths.get("README.md");

    private final BookkeeperConfig config;
    private final ScenamaticaClassLoader classLoader;
    private final AnnotationClassifier classifier;
    private final List<IAnnotationReader<? extends IDefinition>> processors;
    private final Compiler compiler;
    private final CategoryManager categoryManager;
    private final ArtifactPacker packer;

    private final Path tempDir;

    public BookkeeperCore(BookkeeperConfig config)
    {
        log.info("Initializing Bookkeeper Core...");
        createOutputDir(config.getOutputDir());
        this.tempDir = createTempDir(config.isDebug(), config.getOutputDir());

        this.config = config;
        this.processors = new ArrayList<>();
        this.classLoader = ScenamaticaClassLoader.create(config.getTargetJar(), this.processors);
        this.classifier = new AnnotationClassifier(this.processors);
        this.categoryManager = new CategoryManager(this.classLoader, this.config.getOutputDir().resolve("categories"));
        this.compiler = new Compiler(this);
        this.packer = new ArtifactPacker(
                new Path[]{this.tempDir},
                !config.isDebug(),
                config.getArtifactCompressionLevel()
        );

        this.classLoader.addClasspaths(this.config.getClassPaths());
        this.addProcessors(this.processors);

        this.compiler.init();

        log.info("Bookkeeper Core initialized.");
    }

    public void start()
    {
        log.info("Starting Bookkeeping...");
        copyLicenseAndReadme(this.config.getOutputDir());

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

        log.info("Packing artifact...");
        this.packer.pack(this.config.getOutputDir(), this.config.getOutputDir().resolve(this.config.getArtifactFileName()));

        this.cleanTempDir();
        log.info("Bookkeeping finished.");
    }

    private void cleanTempDir()
    {
        log.info("Cleaning temporary directory...");
        try
        {
            Files.walk(this.tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            Files.deleteIfExists(this.tempDir);
        }
        catch (IOException e)
        {
            log.error("Failed to clean temporary directory: {}", this.tempDir, e);
        }
    }

    private void addProcessors(List<? super IAnnotationReader<? extends IDefinition>> registry)
    {
        OutputDefinitionReader outputDefinitionReader = new OutputDefinitionReader();
        InputDefinitionReader inputDefinitionReader = new InputDefinitionReader();
        ActionDefinitionReader actionDefinitionReader = new ActionDefinitionReader(this.classLoader, inputDefinitionReader, outputDefinitionReader);
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

    private static void copyLicenseAndReadme(Path outputDir)
    {
        try (InputStream licenseStream = BookkeeperCore.class.getClassLoader().getResourceAsStream(LICENSE_FILE.toString());
             InputStream readmeStream = BookkeeperCore.class.getClassLoader().getResourceAsStream(README_FILE.toString()))
        {
            if (licenseStream == null || readmeStream == null)
                throw new IllegalStateException("Failed to load license and(or) readme files");

            Files.copy(licenseStream, outputDir.resolve(LICENSE_FILE));
            Files.copy(readmeStream, outputDir.resolve(README_FILE));
        }
        catch (NullPointerException e)
        {
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to copy license and readme files", e);
        }
    }

    private static Path createTempDir(boolean debug, Path baseDir)
    {
        try
        {
            Path tempDir;
            tempDir = baseDir.resolve("temp");
            Files.createDirectories(tempDir);

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
                .forEach(f -> {
                    if (!f.delete())
                        throw new IllegalStateException("Failed to delete file: " + f);
                });

        Files.deleteIfExists(outputDir);
        if (Files.exists(outputDir))
            throw new IOException("Failed to delete output directory: " + outputDir);
    }
}
