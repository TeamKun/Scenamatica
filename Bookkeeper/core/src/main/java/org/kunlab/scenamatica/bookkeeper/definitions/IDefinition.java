package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

public interface IDefinition
{
    Class<?> getAnnotationType();

    boolean isRelatedTo(@NotNull ClassNode classNode);
}
