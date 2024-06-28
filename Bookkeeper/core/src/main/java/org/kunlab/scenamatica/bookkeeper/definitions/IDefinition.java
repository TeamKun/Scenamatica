package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.objectweb.asm.tree.ClassNode;

public interface IDefinition
{
    ClassNode getAnnotatedClass();

    Class<?> getAnnotationType();

    boolean isDependsOn(@Nullable ScenamaticaClassLoader classLoader, @NotNull IDefinition others);
}
