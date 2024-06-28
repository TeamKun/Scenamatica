package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.objectweb.asm.tree.ClassNode;

@Value
public class ActionCategoryDefinition implements IDefinition
{
    ClassNode annotatedClass;
    String id;
    String name;
    String description;
    boolean inherit;

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
    public boolean isDependsOn(@NotNull ScenamaticaClassLoader classLoader, @NotNull IDefinition classNode)
    {
        return false;
    }
}
