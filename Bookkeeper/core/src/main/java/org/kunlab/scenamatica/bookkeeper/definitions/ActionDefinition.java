package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;

@Value
public class ActionDefinition implements IDefinition
{
    ClassNode clazz;
    String name;
    String description;
    Type[] events;
    String executable;
    String watchable;
    String requireable;
    MCVersion supportsSince;
    MCVersion supportsUntil;
    OutputDefinition[] outputs;

    @Nullable
    ActionDefinition parent;

    @Override
    public Class<?> getAnnotationType()
    {
        return ActionDoc.class;
    }

    @Override
    public boolean isRelatedTo(@NotNull ClassNode classNode)
    {
        if (this.events != null)
        {
            boolean isEventsRelated = Arrays.stream(this.events).parallel()
                    .anyMatch(name -> name.getClassName().equals(classNode.name));

            if (isEventsRelated)
                return true;
        }

        if (this.outputs != null)
            return Arrays.stream(this.outputs).parallel()
                    .anyMatch(output -> output.isRelatedTo(classNode));

        return false;
    }
}
