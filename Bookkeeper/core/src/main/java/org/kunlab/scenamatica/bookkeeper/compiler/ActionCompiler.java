package org.kunlab.scenamatica.bookkeeper.compiler;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionCompiler extends AbstractCompiler<ActionDefinition, CompiledAction, ActionReference>
{
    private final TypeCompiler typeCompiler;
    private final EventCompiler eventCompiler;
    private final CategoryManager categoryManager;

    public ActionCompiler(TypeCompiler typeCompiler, EventCompiler eventCompiler, CategoryManager categoryManager)
    {
        super("actions");
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

    private Map<String, CompiledAction.ActionInput> compileInputs(ActionDefinition definition)
    {
        if (definition.getInputs() == null)
            return new HashMap<>();

        Map<String, CompiledAction.ActionInput> inputs = new HashMap<>();

        for (InputDefinition input : definition.getInputs())
        {
            CompiledAction.ActionInput compiled = new CompiledAction.ActionInput(
                    input.getName(),
                    input.getDescription(),
                    input.getRequiredOn(),
                    input.getAvailableFor(),
                    input.getSupportsSince(),
                    input.getSupportsUntil(),
                    input.getMin(),
                    input.getMax(),
                    input.getConstValue(),
                    input.isRequiresActor()
            );
            inputs.put(input.getName(), compiled);
        }

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
