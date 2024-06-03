package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Objects;

@Value
public class OutputDefinition implements IDefinition
{
    String name;
    String description;
    ActionMethod[] targets;
    Type type;
    String supportsSince;
    String supportsUntil;

    @Override
    public Class<?> getAnnotationType()
    {
        return OutputDoc.class;
    }

    @Override
    public boolean isRelatedTo(@NotNull ClassNode classNode)
    {
        return (this.type != null && Objects.equals(this.type.getClassName(), classNode.name));
    }
}
