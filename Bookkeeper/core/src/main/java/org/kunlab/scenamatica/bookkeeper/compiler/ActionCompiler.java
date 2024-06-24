package org.kunlab.scenamatica.bookkeeper.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationClassifier;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledAction;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.ActionReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.EventReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.InputDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionCompiler extends AbstractCompiler<ActionDefinition, CompiledAction, ActionReference>
{
    private final ScenamaticaClassLoader classLoader;
    private final AnnotationClassifier classifier;
    private final TypeCompiler typeCompiler;
    private final EventCompiler eventCompiler;
    private final CategoryManager categoryManager;

    public ActionCompiler(ScenamaticaClassLoader classLoader, AnnotationClassifier classifier, TypeCompiler typeCompiler,
                          EventCompiler eventCompiler, CategoryManager categoryManager)
    {
        super("actions");
        this.classLoader = classLoader;
        this.classifier = classifier;
        this.typeCompiler = typeCompiler;
        this.eventCompiler = eventCompiler;
        this.categoryManager = categoryManager;
    }

    @Override
    public Class<ActionDefinition> getDefinitionType()
    {
        return ActionDefinition.class;
    }

    @Override
    protected String toId(CompiledAction compiledItem)
    {
        return compiledItem.getId();
    }

    @Override
    protected ActionReference doCompile(ActionDefinition definition)
    {
        List<EventReference> events = new ArrayList<>();
        if (definition.getEvents() != null)
        {
            if (this.eventCompiler == null)
                for (Type eventType : definition.getEvents())
                    events.add(new NameOnlyEventReference(eventType.getInternalName()));
            else
                for (Type event : definition.getEvents())
                    events.add(this.eventCompiler.resolve(event.getClassName()));
        }

        CompiledAction.Contract executable = compileContract(definition.getExecutable());
        CompiledAction.Contract watchable = compileContract(definition.getWatchable());
        CompiledAction.Contract requireable = compileContract(definition.getRequireable());

        ClassNode clazz = definition.getClazz();

        CategoryManager.CategoryEntry categoryReference;
        if (definition.getId().equals("server_log")) // サーバ・ログは特別に扱う。
            categoryReference = this.categoryManager.getCategoryByID("server");
        else
            categoryReference = this.categoryManager.recogniseCategory(clazz);

        return new ActionReference(
                new CompiledAction(
                        definition.getClazz(),
                        definition.getId(),
                        definition.getName(),
                        definition.getDescription(),
                        categoryReference,
                        events.toArray(new EventReference[0]),
                        executable,
                        watchable,
                        requireable,
                        definition.getSupportsSince(),
                        definition.getSupportsUntil(),
                        compileInputs(definition),
                        compileOutputs(definition)
                )
        );
    }

    @NotNull
    private List<CompiledAction.ActionInput> getSuperActions(ClassNode node)
    {
        List<CompiledAction.ActionInput> superActions = new ArrayList<>();
        if (!canTraceSuper(node))
            return Collections.emptyList();

        ClassNode superClass = this.classLoader.getClassByName(node.superName);
        while (superClass != null)
        {
            AnnotationClassifier.ClassifiedAnnotation annotation = this.classifier.getClassfieidAnnotations(superClass);
            if (annotation == null)
            {
                if (!canTraceSuper(superClass))
                    break;

                superClass = this.classLoader.getClassByName(superClass.superName);
                continue;
            }

            ActionDefinition actionDefinition = annotation.getAnnotations().stream()
                    .filter(a -> a instanceof ActionDefinition)
                    .map(a -> (ActionDefinition) a)
                    .findFirst()
                    .orElse(null);
            if (actionDefinition == null)
            {
                if (!canTraceSuper(superClass))
                    break;

                superClass = this.classLoader.getClassByName(superClass.superName);
                continue;
            }

            ActionReference inheritedReference = this.compiledItemReferences.get(actionDefinition.getId());
            for (InputDefinition input : actionDefinition.getInputs())
                superActions.add(constructInput(input, inheritedReference));

            if (canTraceSuper(superClass))
                superClass = this.classLoader.getClassByName(superClass.superName);
            else
                break;
        }

        return superActions;
    }

    private Map<String, CompiledAction.ActionInput> compileInputs(ActionDefinition definition)
    {
        Map<String, CompiledAction.ActionInput> inputs = new HashMap<>();
        List<CompiledAction.ActionInput> superActions = getSuperActions(definition.getClazz());
        for (CompiledAction.ActionInput superAction : superActions)
            inputs.put(superAction.getName(), superAction);

        for (InputDefinition input : definition.getInputs())
            inputs.put(input.getName(), constructInput(input, null));

        return inputs;
    }

    private Map<String, CompiledAction.ActionOutput> compileOutputs(ActionDefinition definition)
    {
        if (definition.getOutputs() == null)
            return new HashMap<>();

        Map<String, CompiledAction.ActionOutput> outputs = new HashMap<>();

        for (OutputDefinition output : definition.getOutputs())
        {
            Type outputType = output.getType();
            TypeReference typeReference = this.typeCompiler.lookup(outputType);
            if (typeReference == null)
                throw new IllegalStateException("Type not found: " + outputType.getClassName());

            CompiledAction.ActionOutput compiled = new CompiledAction.ActionOutput(
                    output.getName(),
                    output.getDescription(),
                    output.getTargets(),
                    typeReference,
                    output.getSupportsSince(),
                    output.getSupportsUntil(),
                    output.getMin(),
                    output.getMax()
            );
            outputs.put(output.getName(), compiled);
        }

        return outputs;
    }

    @Override
    protected String toId(ActionDefinition definition)
    {
        return definition.getId();
    }

    @Override
    protected String getFileLocation(ActionReference reference)
    {
        String sup = super.getFileLocation(reference);
        CategoryManager.CategoryEntry categoryReference = reference.getResolved().getCategory();
        if (categoryReference == null)
            return sup;

        return categoryReference.getChildrenPath().resolve(sup).toString();
    }

    private ActionReference getActionByClass(ClassNode clazz)
    {
        return this.compiledItemReferences.values()
                .stream()
                .filter(actionReference -> actionReference.getResolved().getClazz().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    private static boolean canTraceSuper(ClassNode node)
    {
        return !(node.superName == null || node.superName.equals("java/lang/Object") || node.superName.equals("java/lang/Enum"));
    }

    private static CompiledAction.ActionInput constructInput(InputDefinition input, @Nullable ActionReference inherits)
    {
        return new CompiledAction.ActionInput(
                input.getName(),
                input.getDescription(),
                input.getRequiredOn(),
                input.getAvailableFor(),
                input.getSupportsSince(),
                input.getSupportsUntil(),
                input.getMin(),
                input.getMax(),
                input.getConstValue(),
                input.isRequiresActor(),
                inherits
        );
    }

    private static CompiledAction.Contract compileContract(String string)
    {
        if (string == null || string.equals(ActionDoc.UNALLOWED))
            return CompiledAction.Contract.ofUnavailable();
        else
            return CompiledAction.Contract.ofAvailable(string);

    }

    private static class NameOnlyEventReference extends EventReference
    {
        public NameOnlyEventReference(String name)
        {
            super(name, null);
        }

        @Override
        public String getReference()
        {
            return "$ref:event:?" + this.id;
        }
    }
}
