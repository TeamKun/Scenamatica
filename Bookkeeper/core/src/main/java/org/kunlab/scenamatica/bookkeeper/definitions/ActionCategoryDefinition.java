package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionCategory;
import org.objectweb.asm.tree.ClassNode;

@Value
public class ActionCategoryDefinition implements IDefinition
{
    String id;
    String name;
    String description;
    boolean inherit;

    @Override
    public Class<?> getAnnotationType()
    {
        return ActionCategory.class;
    }

    @Override
    public boolean isRelatedTo(@NotNull ClassNode classNode)
    {
        return false;
    }
}
