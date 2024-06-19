package org.kunlab.scenamatica.bookkeeper.compiler;

import org.kunlab.scenamatica.bookkeeper.AnnotationClassifier;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledCategory;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.CategoryReference;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionCategoryDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionDefinition;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CategoryCompiler extends AbstractCompiler<ActionCategoryDefinition, CompiledCategory, CategoryReference>
{
    private final AnnotationClassifier classifier;
    private final ScenamaticaClassLoader loader;

    public CategoryCompiler(AnnotationClassifier classifier, ScenamaticaClassLoader loader)
    {
        super("category");
        this.classifier = classifier;
        this.loader = loader;
    }

    public CategoryReference lookupCategory(ClassNode clazz)
    {
        AnnotationClassifier.ClassifiedAnnotation classified = this.classifier.getClassfieidAnnotations(clazz);
        if (classified == null)
            return null;

        ActionDefinition definition = classified.getAnnotations().stream()
                .filter(a -> a.getAnnotationType().equals(Category.class))
                .map(a -> (ActionDefinition) a)
                .findFirst()
                .orElse(null);
        if (definition == null)
            return null;

        String id = definition.getId();
        return this.compiledItemReferences.get(id);
    }

    @Override
    protected String toId(CompiledCategory compiledItem)
    {
        return compiledItem.getId();
    }

    @Override
    protected CategoryReference doCompile(ActionCategoryDefinition definition)
    {
        String id = definition.getId();
        if (this.compiledItemReferences.containsKey(id)
                && !definition.isInherit())
            throw new IllegalStateException("Duplicate apex of category: " + id + ", consider using inherit = true in the annotation.");

        ClassNode cn = definition.getAnnotatedClass();
        List<ClassNode> nodes = this.traverseHierarchy(cn);
        List<ActionCategoryDefinition> definitions = this.traverseAnnotationHierarchy(nodes);

        ActionCategoryDefinition inherited = this.inheritAll(definitions);
        return new CategoryReference(new CompiledCategory(
                id,
                inherited.getName(),
                inherited.getDescription()
        ));
    }

    private ActionCategoryDefinition inheritAll(List<ActionCategoryDefinition> definitions)
    {
        if (definitions.isEmpty())
            throw new IllegalStateException("No definitions to inherit from");

        ActionCategoryDefinition definition = definitions.remove(0);

        if (!definition.isInherit())
            return definition;

        String id = definition.getId();
        String name = definition.getName();
        String description = definition.getDescription();
        ActionCategoryDefinition inherited = null;
        for (ActionCategoryDefinition def : definitions)
        {
            if (!def.getId().equals(id) || !def.isInherit())
                break;

            if (!Objects.equals(def.getName(), name))
                name = def.getName();
            if (!Objects.equals(def.getDescription(), description))
                description = def.getDescription();

            AnnotationClassifier.ClassifiedAnnotation cl = this.classifier.getClassfieidAnnotations(def.getAnnotatedClass());
            cl.removeDefinition(def);
            cl.addAnnotation(inherited = new ActionCategoryDefinition(def.getAnnotatedClass(), id, name, description, false));
        }

        return inherited == null ? definition: inherited;
    }

    private List<ActionCategoryDefinition> traverseAnnotationHierarchy(List<? extends ClassNode> nodes)
    {
        List<ActionCategoryDefinition> definitions = new LinkedList<>();
        for (ClassNode node : nodes)
        {
            AnnotationClassifier.ClassifiedAnnotation definition = this.classifier.getClassfieidAnnotations(node);
            if (definition == null)
                continue;
            List<ActionCategoryDefinition> categoryDefs = definition.getAnnotations().stream()
                    .filter(a -> a.getAnnotationType().equals(Category.class))
                    .map(a -> (ActionCategoryDefinition) a)
                    .collect(Collectors.toList());

            definitions.addAll(categoryDefs);
        }

        return definitions;
    }

    private List<ClassNode> traverseHierarchy(ClassNode cn)
    {
        List<ClassNode> nodes = new ArrayList<>();
        nodes.add(cn);

        String superName = cn.superName;
        while (superName != null)
        {
            ClassNode superNode = this.loader.getClassByName(superName);
            if (superNode == null)
                throw new IllegalStateException("Super class not found: " + superName + " for classes: "
                        + nodes.stream().map(n -> n.name).reduce((a, b) -> a + " -> " + b).orElse(""));
            nodes.add(superNode);
            superName = superNode.superName;
        }

        return nodes;
    }

    @Override
    protected String toId(ActionCategoryDefinition definition)
    {
        return definition.getId();
    }

    @Override
    public Class<ActionCategoryDefinition> getDefinitionType()
    {
        return ActionCategoryDefinition.class;
    }
}
