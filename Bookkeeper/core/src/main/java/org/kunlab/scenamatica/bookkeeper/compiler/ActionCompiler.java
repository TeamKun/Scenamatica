package org.kunlab.scenamatica.bookkeeper.compiler;

import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledAction;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.ActionReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.CategoryReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.EventReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.InputDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionCompiler extends AbstractCompiler<ActionDefinition, CompiledAction, ActionReference>
{
    private final TypeCompiler typeCompiler;
    private final EventCompiler eventCompiler;
    private final CategoryCompiler categoryCompiler;

    public ActionCompiler(TypeCompiler typeCompiler, EventCompiler eventCompiler, CategoryCompiler categoryCompiler)
    {
        super("action");
        this.typeCompiler = typeCompiler;
        this.eventCompiler = eventCompiler;
        this.categoryCompiler = categoryCompiler;
    }

    @Override
    public Class<ActionDefinition> getDefinitionType()
    {
        return ActionDefinition.class;
    }

    @Override
    protected String toId(CompiledAction compiledItem)
    {
        return compiledItem.id();
    }

    @Override
    protected ActionReference doCompile(ActionDefinition definition)
    {
        List<EventReference> events = new ArrayList<>();
        if (!(this.eventCompiler == null || definition.events() == null))
            for (Type event : definition.events())
                events.add(this.eventCompiler.resolve(event.getClassName()));

        CompiledAction.Contract executable = compileContract(definition.executable());
        CompiledAction.Contract watchable = compileContract(definition.watchable());
        CompiledAction.Contract requireable = compileContract(definition.requireable());

        ClassNode clazz = definition.clazz();

        CategoryReference categoryReference = this.categoryCompiler.lookupCategory(clazz);

        return new ActionReference(
                new CompiledAction(
                        definition.id(),
                        definition.name(),
                        definition.description(),
                        categoryReference,
                        events.toArray(new EventReference[0]),
                        executable,
                        watchable,
                        requireable,
                        definition.supportsSince(),
                        definition.supportsUntil(),
                        compileInputs(definition),
                        compileOutputs(definition)
                )
        );
    }

    private Map<String, CompiledAction.ActionInput> compileInputs(ActionDefinition definition)
    {
        if (definition.inputs() == null)
            return new HashMap<>();

        Map<String, CompiledAction.ActionInput> inputs = new HashMap<>();

        for (InputDefinition input : definition.inputs())
        {
            CompiledAction.ActionInput compiled = new CompiledAction.ActionInput(
                    input.name(),
                    input.description(),
                    input.requiredOn(),
                    input.availableFor(),
                    input.supportsSince(),
                    input.supportsUntil(),
                    input.min(),
                    input.max(),
                    input.constValue(),
                    input.requiresActor()
            );
            inputs.put(input.name(), compiled);
        }

        return inputs;
    }

    private Map<String, CompiledAction.ActionOutput> compileOutputs(ActionDefinition definition)
    {
        if (definition.outputs() == null)
            return new HashMap<>();

        Map<String, CompiledAction.ActionOutput> outputs = new HashMap<>();

        for (OutputDefinition output : definition.outputs())
        {
            Type outputType = output.type();
            TypeReference typeReference = this.typeCompiler.resolve(TypeCompiler.classNameToId(outputType.getClassName()));
            if (typeReference == null)
                throw new IllegalStateException("Type not found: " + outputType.getClassName());

            CompiledAction.ActionOutput compiled = new CompiledAction.ActionOutput(
                    output.name(),
                    output.description(),
                    output.targets(),
                    typeReference,
                    output.supportsSince(),
                    output.supportsUntil(),
                    output.min(),
                    output.max()
            );
            outputs.put(output.name(), compiled);
        }

        return outputs;
    }

    @Override
    protected String toId(ActionDefinition definition)
    {
        return definition.id();
    }

    private static CompiledAction.Contract compileContract(String string)
    {
        if (string.equals(ActionDoc.UNALLOWED))
            return CompiledAction.Contract.ofUnavailable();
        else
            return CompiledAction.Contract.ofAvailable(string);

    }
}
