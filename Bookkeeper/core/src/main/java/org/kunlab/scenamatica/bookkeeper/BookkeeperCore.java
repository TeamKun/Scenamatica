package org.kunlab.scenamatica.bookkeeper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.kunlab.scenamatica.bookkeeper.reader.ActionCategoryDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.ActionDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.IAnnotationReader;
import org.kunlab.scenamatica.bookkeeper.reader.OutputDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.TypeDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.reader.TypePropertyDefinitionReader;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j(topic = "Bookkeeper/Core")
public class BookkeeperCore
{
    private final BookkeeperConfig config;
    private final ScenamaticaClassLoader classLoader;
    private final AnnotationClassifier classifier;
    private final List<IAnnotationReader<? extends IDefinition>> processors;

    public BookkeeperCore(BookkeeperConfig config)
    {
        this.config = config;
        this.processors = new ArrayList<>();
        this.classLoader = ScenamaticaClassLoader.create(config.getTargetJar(), this.processors);
        this.classifier = new AnnotationClassifier(this.processors);

        this.classLoader.addClasspaths(this.config.getClassPaths());
        addProcessors(this.processors);
    }

    public void start()
    {
        log.info("Starting Bookkeeping...");

        log.info("Scanning classes...");
        this.classLoader.scanAll(this.config.getThreads());

        log.info("Looking up for annotations...");
        this.classifier.classify(this.classLoader.getScenamaticaClasses());

        log.info("Rendering classes...");


        log.info("Bookkeeping finished.");
    }

    private boolean isUnusedClass(ClassNode node)
    {
        AnnotationClassifier.ClassifiedAnnotation classified = this.classifier.getClassfieidAnnotations(node);
        return classified == null || classified.getAnnotations().stream().noneMatch(d -> d.isRelatedTo(node));
    }

    private static void addProcessors(List<? super IAnnotationReader<? extends IDefinition>> registry)
    {
        OutputDefinitionReader outputDefinitionReader = new OutputDefinitionReader();
        ActionDefinitionReader actionDefinitionReader = new ActionDefinitionReader(outputDefinitionReader);
        TypePropertyDefinitionReader propertyDefinitionReader = new TypePropertyDefinitionReader();
        TypeDefinitionReader typeDefinitionReader = new TypeDefinitionReader(propertyDefinitionReader);
        ActionCategoryDefinitionReader actionCategoryDefinitionReader = new ActionCategoryDefinitionReader();

        registry.add(outputDefinitionReader);
        registry.add(actionDefinitionReader);
        registry.add(propertyDefinitionReader);
        registry.add(typeDefinitionReader);
        registry.add(actionCategoryDefinitionReader);
    }

    public static void main(String[] args)
    {
        BookkeeperConfig config = BookkeeperConfig.builder()
                .targetJar(Paths.get("D:\\projects\\kun\\scenamatica\\Scenamatica\\ScenamaticaPlugin\\target\\Scenamatica-1.4.1.jar"))
                .outputDir(Paths.get("dist"))
                .classPath(PaperClassPathFinder.findLocalRepositoryJar("1.16.5"))
                .build();
        BookkeeperCore core = new BookkeeperCore(config);
        core.start();
    }
}
