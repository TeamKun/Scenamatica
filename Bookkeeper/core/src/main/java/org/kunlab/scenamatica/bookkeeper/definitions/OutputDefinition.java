package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

@Value
public class OutputDefinition implements IDefinition
{
    ClassNode annotatedClass;
    String name;
    String description;
    ActionMethod[] targets;
    Type type;
    MCVersion supportsSince;
    MCVersion supportsUntil;
    double min;
    double max;

    @Override
    public ClassNode getAnnotatedClass()
    {
        return this.annotatedClass;
    }

    @Override
    public Class<?> getAnnotationType()
    {
        return OutputDoc.class;
    }

    @Override
    public boolean isDependsOn(@NotNull IDefinition def)
    {
        return (this.type != null && this.type.getInternalName().equals(def.getAnnotatedClass().name));
    }
}
