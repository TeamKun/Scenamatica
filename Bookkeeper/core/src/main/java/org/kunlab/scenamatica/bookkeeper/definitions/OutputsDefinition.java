package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.objectweb.asm.tree.ClassNode;

public record OutputsDefinition(ClassNode clazz, OutputDefinition[] outputs) implements IDefinition
{
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
