package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

@Value
@NotNull
public class TypePropertyDefinition implements IDefinition
{
    String name;
    String description;
    Type type;
    boolean required;
    String defaultValue;
    String[] values;
    Type mappingOf;

    @Override
    public Class<?> getAnnotationType()
    {
        return TypeProperty.class;
    }

    @Override
    public boolean isRelatedTo(@NotNull ClassNode classNode)
    {
        return this.mappingOf != null && classNode.name.equals(this.mappingOf.getInternalName());
    }
}
