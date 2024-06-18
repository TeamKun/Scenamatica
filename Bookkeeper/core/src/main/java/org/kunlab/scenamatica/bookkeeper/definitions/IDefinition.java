package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

public interface IDefinition
{
    ClassNode getAnnotatedClass();

    Class<?> getAnnotationType();

    boolean isDependsOn(@NotNull IDefinition others);
}
