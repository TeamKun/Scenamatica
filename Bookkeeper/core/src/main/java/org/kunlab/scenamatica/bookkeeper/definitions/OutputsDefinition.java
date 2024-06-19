package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.objectweb.asm.tree.ClassNode;

@Value
public class OutputsDefinition implements IDefinition
{
    ClassNode clazz;
    OutputDefinition[] outputs;

    @Override
    public ClassNode getAnnotatedClass()
    {
        return this.clazz;
    }

    @Override
    public Class<?> getAnnotationType()
    {
        return OutputDocs.class;
    }

    @Override
    public boolean isDependsOn(@NotNull IDefinition classNode)
    {
        return false;
    }
}
