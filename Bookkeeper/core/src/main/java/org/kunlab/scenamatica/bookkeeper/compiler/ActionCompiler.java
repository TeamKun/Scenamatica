package org.kunlab.scenamatica.bookkeeper.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationClassifier;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledAction;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.ActionReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.EventReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.InputDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputsDefinition;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.reader.IAnnotationReader;
import org.kunlab.scenamatica.bookkeeper.reader.InputDefinitionReader;
import org.kunlab.scenamatica.bookkeeper.utils.ClassAnalyser;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ActionCompiler extends AbstractCompiler<ActionDefinition, CompiledAction, ActionReference>
{
    private static final String DESC_INPUTS = Descriptors.getDescriptor(InputDoc.class);

    private final InputDefinitionReader inputReader;
    private final ScenamaticaClassLoader classLoader;
    private final AnnotationClassifier classifier;
    private final TypeCompiler typeCompiler;
    private final EventCompiler eventCompiler;
    private final CategoryManager categoryManager;

    private final Map<ClassNode, List<CompiledAction.ActionInput>> nonActionInputCaches;

    public ActionCompiler(InputDefinitionReader inputReader, ScenamaticaClassLoader classLoader, AnnotationClassifier classifier, TypeCompiler typeCompiler, EventCompiler eventCompiler, CategoryManager categoryManager)
    {
        super("actions");
        this.inputReader = inputReader;
        this.classLoader = classLoader;
        this.classifier = classifier;
        this.typeCompiler = typeCompiler;
        this.eventCompiler = eventCompiler;
        this.categoryManager = categoryManager;

        this.nonActionInputCaches = new HashMap<>();
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
        ClassNode clazz = definition.getClazz();
        List<ClassNode> superClasses = ClassAnalyser.getSuperClasses(this.classLoader, clazz);
        List<ActionReference> superActions = getActionsFromClasses(superClasses);
        ActionReference superAction = superActions.stream().findFirst().orElse(null);

        List<EventReference> events = new ArrayList<>();
        if (!(definition.getEvents() == null || definition.getEvents().length == 0))
        {
            for (Type event : definition.getEvents())
            {
                String className = event.getClassName();
                if (this.eventCompiler != null && this.eventCompiler.hasEvent(className))
                    events.add(this.eventCompiler.resolve(className));
                else
                    events.add(new NameOnlyEventReference(className));
            }
        }

        /* イベントは継承しないこととする。
        for (ActionReference superActionReference: superActions)
        {
            CompiledAction superCompiled = superActionReference.getResolved();
            if (superCompiled.getEvents() != null)
                events.addAll(Arrays.asList(superCompiled.getEvents()));
        }*/

        CompiledAction.Contract executable = CompiledAction.Contract.ofUnavailable();
        CompiledAction.Contract expectable = CompiledAction.Contract.ofUnavailable();
        CompiledAction.Contract requireable = CompiledAction.Contract.ofUnavailable();
        String executableQuery;
        if (hasValidContract(executableQuery = definition.getExecutable()))
            executable = compileContract(executableQuery);
        else
        {
            CompiledAction.Contract superExecutable = findSuperDeclaration(superActions, CompiledAction::getExecutable, CompiledAction.Contract::isAvailable);
            if (superExecutable != null)
                executable = superExecutable;
        }

        String expectableQuery;
        if (hasValidContract(expectableQuery = definition.getExpectable()))
            expectable = compileContract(expectableQuery);
        else
        {
            CompiledAction.Contract superExpectable = findSuperDeclaration(superActions, CompiledAction::getExpectable, CompiledAction.Contract::isAvailable);
            if (superExpectable != null)
                expectable = superExpectable;
        }

        String requireableQuery;
        if (hasValidContract(requireableQuery = definition.getRequireable()))
            requireable = compileContract(requireableQuery);
        else
        {
            CompiledAction.Contract superRequireable = findSuperDeclaration(superActions, CompiledAction::getRequireable, CompiledAction.Contract::isAvailable);
            if (superRequireable != null)
                requireable = superRequireable;
        }

        MCVersion supportsSince = definition.getSupportsSince();
        MCVersion supportsUntil = definition.getSupportsUntil();
        if (supportsSince == MCVersion.UNSET)
        {
            MCVersion superSince = findSuperDeclaration(superActions, CompiledAction::getSupportsSince, v -> v != MCVersion.UNSET);
            if (superSince != null)
                supportsSince = superSince;
        }
        if (supportsUntil == MCVersion.UNSET)
        {
            MCVersion superUntil = findSuperDeclaration(superActions, CompiledAction::getSupportsUntil, v -> v != MCVersion.UNSET);
            if (superUntil != null)
                supportsUntil = superUntil;
        }

        CategoryManager.CategoryEntry categoryReference;
        if (definition.getId().equals("server_log")) // サーバ・ログは特別に扱う。
            categoryReference = this.categoryManager.getCategoryByID("servers");
        else
            categoryReference = this.categoryManager.recogniseCategory(clazz);

        return new ActionReference(
                new CompiledAction(
                        definition.getClazz(),
                        definition.getId(),
                        definition.getName(),
                        superAction,
                        definition.getDescription(),
                        categoryReference,
                        events.toArray(new EventReference[0]),
                        executable,
                        expectable,
                        requireable,
                        supportsSince,
                        supportsUntil,
                        compileInputs(definition, superClasses, superActions),
                        compileOutputs(definition, superClasses, superActions),
                        definition.getAdmonitions()
                )
        );
    }

    private <T> T findSuperDeclaration(List<? extends ActionReference> supers, Function<? super CompiledAction, ? extends T> getter, Predicate<? super T> check)
    {
        for (ActionReference superAction : supers)
        {
            CompiledAction superCompiled = superAction.getResolved();
            T value = getter.apply(superCompiled);
            if (check.test(value))
                return value;
        }

        return null;
    }

    @NotNull
    private List<ActionReference> getActionsFromClasses(List<? extends ClassNode> superClasses)
    {
        List<ActionReference> superActions = new ArrayList<>();

        for (ClassNode superClass : superClasses)
        {
            AnnotationClassifier.ClassifiedAnnotation annotation = this.classifier.getClassfieidAnnotations(superClass);
            if (annotation == null)
                continue;

            ActionDefinition actionDefinition = annotation.getAnnotations().stream()
                    .filter(a -> a instanceof ActionDefinition)
                    .map(a -> (ActionDefinition) a)
                    .findFirst()
                    .orElse(null);
            if (actionDefinition == null)
                continue;

            ActionReference inheritedReference = this.compiledItemReferences.get(actionDefinition.getId());
            if (inheritedReference == null)
                throw new IllegalStateException("Super action not found: " + actionDefinition.getId());

            superActions.add(inheritedReference);
        }

        return superActions;
    }

    public Map<String, CompiledAction.ActionInput> processNonActionInputs(List<? extends ClassNode> superClasses, List<? extends ActionReference> supers)
    {
        Map<String, CompiledAction.ActionInput> inputs = new HashMap<>();
        List<CompiledAction.ActionInput> newInputs = new ArrayList<>();
        for (ClassNode superClass : superClasses)
        {
            if (isDuplicatedActionReference(superClass, supers))
                continue;
            else if (this.nonActionInputCaches.containsKey(superClass))
            {
                for (CompiledAction.ActionInput input : this.nonActionInputCaches.get(superClass))
                    inputs.put(input.getName(), input);
                continue;
            }

            superClass.fields.stream()
                    .filter(getAnnotatedFieldsPredicate(DESC_INPUTS, this.inputReader))
                    .map(f -> processInputField(superClass, f))
                    .forEach(i -> {
                        inputs.putAll(i);
                        newInputs.addAll(i.values());
                    });
        }

        for (ClassNode superClass : superClasses)
            this.nonActionInputCaches.put(superClass, newInputs);

        return inputs;
    }

    private boolean isDuplicatedActionReference(ClassNode superClass, List<? extends ActionReference> supers)
    {
        return supers.stream().anyMatch(a -> a.getResolved().getClazz().equals(superClass));
    }

    private Predicate<FieldNode> getAnnotatedFieldsPredicate(String desc, IAnnotationReader<?> reader)
    {
        Predicate<FieldNode> isPublic = f -> (f.access & Opcodes.ACC_PUBLIC) != 0;
        Predicate<FieldNode> isFinal = f -> (f.access & Opcodes.ACC_FINAL) != 0;
        Predicate<FieldNode> isValidAccess = isPublic.and(isFinal);

        Predicate<FieldNode> hasAnnotation = f -> f.invisibleAnnotations != null;
        Predicate<FieldNode> hasInputAnnotation = f -> f.invisibleAnnotations.stream().anyMatch(a -> a.desc.equals(desc) && reader.canRead(a));

        return isValidAccess.and(hasAnnotation).and(hasInputAnnotation);
    }

    private Map<String, CompiledAction.ActionInput> processInputField(ClassNode superClass, FieldNode field)
    {
        Map<String, CompiledAction.ActionInput> inputs = new HashMap<>();
        for (AnnotationNode anno : field.invisibleAnnotations)
        {
            AnnotationValues values = AnnotationValues.of(anno);
            InputDefinition input = this.inputReader.buildAnnotation(superClass, values);
            TypeReference type = this.typeCompiler.lookup(input.getType());
            inputs.put(input.getName(), constructInput(input, type, getActionByClass(superClass)));
        }

        return inputs;
    }

    private Map<String, CompiledAction.ActionInput> compileInputs(ActionDefinition definition, List<? extends ClassNode> superClasses, List<? extends ActionReference> supers)
    {
        // merge super classes
        Map<String, CompiledAction.ActionInput> inputs = new HashMap<>(this.processNonActionInputs(superClasses, supers));

        // merge supers
        for (ActionReference superAction : supers)
        {
            CompiledAction superCompiled = superAction.getResolved();
            for (Map.Entry<String, CompiledAction.ActionInput> entry : superCompiled.getInputs().entrySet())
                inputs.put(entry.getKey(), CompiledAction.ActionInput.inherit(entry.getValue(), superAction));
        }

        for (InputDefinition input : definition.getInputs())
        {
            TypeReference typeReference = this.typeCompiler.lookup(input.getType());
            inputs.put(input.getName(), constructInput(input, typeReference, null));
        }

        return inputs;
    }

    private Map<String, CompiledAction.ActionOutput> compileOutputs(ActionDefinition definition, List<? extends ClassNode> superClasses, List<? extends ActionReference> supers)
    {
        Map<String, CompiledAction.ActionOutput> outputs = new HashMap<>();
        // merge supers
        for (ActionReference superAction : supers)
        {
            CompiledAction superCompiled = superAction.getResolved();
            for (Map.Entry<String, CompiledAction.ActionOutput> entry : superCompiled.getOutputs().entrySet())
                outputs.put(entry.getKey(), CompiledAction.ActionOutput.inherit(entry.getValue(), superAction));
        }

        List<OutputDefinition> outputDefs = new ArrayList<>();
        if (definition.getOutputs() != null)
            outputDefs.addAll(Arrays.asList(definition.getOutputs()));

        outputDefs.addAll(getExternalOutputs(superClasses));

        for (OutputDefinition output : outputDefs)
        {
            Type outputType = output.getType();
            TypeReference typeReference = this.typeCompiler.lookup(outputType);
            if (typeReference == null)
                throw new IllegalStateException("Type not found: " + outputType.getClassName());

            CompiledAction.ActionOutput compiled = constructOutput(output, typeReference, null);
            outputs.put(output.getName(), compiled);
        }


        return outputs;
    }

    private List<OutputDefinition> getExternalOutputs(List<? extends ClassNode> supers)
    {
        List<OutputDefinition> outputs = new ArrayList<>();
        Predicate<OutputDefinition> checkOutputNotContains = o -> outputs.stream().noneMatch(out -> out.getName().equals(o.getName()));

        for (ClassNode superClass : supers)
        {
            AnnotationClassifier.ClassifiedAnnotation clano = this.classifier.getClassfieidAnnotations(superClass);
            if (clano == null)
                continue;

            clano.getAnnotations().stream()
                    .filter(a -> a instanceof OutputsDefinition)
                    .map(a -> (OutputsDefinition) a)
                    .findFirst()
                    .ifPresent(a -> Arrays.stream(a.getOutputs())
                            .filter(checkOutputNotContains)
                            .forEach(outputs::add));
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

    private static boolean hasValidContract(String contractQuery)
    {
        return !(contractQuery == null || ActionDoc.UNSET.equals(contractQuery));
    }

    private static CompiledAction.ActionOutput constructOutput(OutputDefinition output, TypeReference typeReference, @Nullable ActionReference inherits)
    {
        return new CompiledAction.ActionOutput(
                output.getName(),
                output.getDescription(),
                output.getTargets(),
                typeReference,
                output.getSupportsSince(),
                output.getSupportsUntil(),
                output.getMin(),
                output.getMax(),
                inherits,
                output.getAdmonitions()
        );
    }

    private static CompiledAction.ActionInput constructInput(InputDefinition input, TypeReference type, @Nullable ActionReference inherits)
    {
        return new CompiledAction.ActionInput(
                input.getName(),
                type,
                input.getDescription(),
                input.getRequiredOn(),
                input.getAvailableFor(),
                input.getSupportsSince(),
                input.getSupportsUntil(),
                input.getMin(),
                input.getMax(),
                input.getConstValue(),
                input.isRequiresActor(),
                inherits,
                input.getAdmonitions()
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
