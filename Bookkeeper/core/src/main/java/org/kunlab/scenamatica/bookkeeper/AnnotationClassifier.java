package org.kunlab.scenamatica.bookkeeper;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.kunlab.scenamatica.bookkeeper.reader.IAnnotationReader;
import org.kunlab.scenamatica.bookkeeper.utils.Timekeeper;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j(topic = "Bookkeeper/AnnotationClassifier")
public class AnnotationClassifier
{
    private final List<? extends IAnnotationReader<? extends IDefinition>> processors;
    private final Map<String, ClassifiedAnnotation> classified;

    public AnnotationClassifier(List<? extends IAnnotationReader<? extends IDefinition>> processors)
    {
        this.processors = processors;
        this.classified = new HashMap<>();
    }

    public List<ClassifiedAnnotation> classify(List<? extends ClassNode> nodes)
    {
        Timekeeper timekeeper = new Timekeeper(log, "CLASSIFY");
        timekeeper.start();
        List<ClassifiedAnnotation> classified = nodes.stream()
                .map(this::classify)
                .collect(Collectors.toList());
        timekeeper.end();

        return classified;
    }

    public ClassifiedAnnotation classify(ClassNode node)
    {
        if (this.classified.containsKey(node.name))
            return this.classified.get(node.name);

        Stream.of(node.visibleAnnotations, node.invisibleAnnotations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .forEach(annotation -> classify(node, annotation));

        return this.classified.get(node.name);
    }

    public ClassifiedAnnotation getClassfieidAnnotations(ClassNode node)
    {
        return this.classified.get(node.name);
    }

    private void classify(ClassNode node, AnnotationNode annotation)
    {
        for (IAnnotationReader<?> processor : this.processors)
        {
            if (!processor.canRead(annotation))
                continue;

            AnnotationValues values = AnnotationValues.of(annotation);
            IDefinition definition = processor.buildAnnotation(node, values);
            log.debug("Found annotation: {} for class {}", definition.getAnnotationType().getCanonicalName(), node.name);
            this.addAnnotation(node, definition);
        }
    }

    private void addAnnotation(ClassNode node, IDefinition definition)
    {
        ClassifiedAnnotation classified = this.classified.computeIfAbsent(node.name, k -> new ClassifiedAnnotation(node));

        classified.addAnnotation(definition);
    }

    @Value
    public static class ClassifiedAnnotation
    {
        ClassNode node;
        List<IDefinition> annotations;

        public ClassifiedAnnotation(ClassNode node)
        {
            this.node = node;
            this.annotations = new ArrayList<>();
        }

        public void addAnnotation(IDefinition annotation)
        {
            this.annotations.add(annotation);
        }

        public void removeDefinition(IDefinition definition)
        {
            this.annotations.remove(definition);
        }
    }
}
