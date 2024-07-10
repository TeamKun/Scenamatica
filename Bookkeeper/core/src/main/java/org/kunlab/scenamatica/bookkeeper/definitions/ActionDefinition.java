package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.compiler.models.GenericAdmonition;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;

@Value
public class ActionDefinition implements IDefinition
{
    @NotNull
    ClassNode clazz;
    String id;
    String name;
    String description;
    Type[] events;
    String executable;
    String expectable;
    String requireable;
    MCVersion supportsSince;
    MCVersion supportsUntil;
    InputDefinition[] inputs;
    OutputDefinition[] outputs;
    GenericAdmonition[] admonitions;

    @Override
    public ClassNode getAnnotatedClass()
    {
        return this.clazz;
    }

    @Override
    public Class<?> getAnnotationType()
    {
        return ActionDoc.class;
    }

    @Override
    public boolean isDependsOn(@Nullable ScenamaticaClassLoader classLoader, @NotNull IDefinition definition)
    {
        assert classLoader != null;

        if (this.events != null)
        {
            boolean isEventsRelated = Arrays.stream(this.events).parallel()
                    .anyMatch(name -> name.getClassName().equals(definition.getAnnotatedClass().name));

            if (isEventsRelated)
                return true;
        }

        if (this.outputs != null)
        {
            boolean isOutputsRelated = Arrays.stream(this.outputs).parallel()
                    .anyMatch(output -> output.isDependsOn(null, definition));
            if (isOutputsRelated)
                return true;
        }
        if (this.inputs != null)
        {
            boolean isInputsRelated = Arrays.stream(this.inputs).parallel()
                    .anyMatch(input -> input.isDependsOn(null, definition));
            if (isInputsRelated)
                return true;
        }

        if (definition instanceof ActionDefinition)
        {
            ActionDefinition action = (ActionDefinition) definition;
            ClassNode actionClass = action.getAnnotatedClass();
            ClassNode current = this.getAnnotatedClass();
            while (current != null)
            {
                if (current.name.equals(actionClass.name))
                    return true;

                if (current.superName == null || current.superName.equals("java/lang/Object") || current.superName.equals("java/lang/Enum"))
                    break;

                current = classLoader.getClassByName(current.superName);
            }
        }

        return false;
    }
}
