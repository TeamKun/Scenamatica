package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.objectweb.asm.tree.ClassNode;

public record ActionCategoryDefinition(ClassNode annotatedClass, String id, String name, String description,
                                       boolean inherit) implements IDefinition
{
    @Override
    public ClassNode getAnnotatedClass()
    {
        return this.annotatedClass;
    }

    @Override
    public Class<?> getAnnotationType()
    {
        return Category.class;
    }

    @Override
    public boolean isDependsOn(@NotNull IDefinition classNode)
    {
        return false;
    }
}
