package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
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
    String watchable;
    String requireable;
    MCVersion supportsSince;
    MCVersion supportsUntil;
    InputDefinition[] inputs;
    OutputDefinition[] outputs;

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
    public boolean isDependsOn(@NotNull IDefinition definition)
    {
        if (this.events != null)
        {
            boolean isEventsRelated = Arrays.stream(this.events).parallel()
                    .anyMatch(name -> name.getClassName().equals(definition.getAnnotatedClass().name));

            if (isEventsRelated)
                return true;
        }

        if (this.outputs != null)
            return Arrays.stream(this.outputs).parallel()
                    .anyMatch(output -> output.isDependsOn(definition));
        if (this.inputs != null)
            return Arrays.stream(this.inputs).parallel()
                    .anyMatch(input -> input.isDependsOn(definition));

        return false;
    }
}
