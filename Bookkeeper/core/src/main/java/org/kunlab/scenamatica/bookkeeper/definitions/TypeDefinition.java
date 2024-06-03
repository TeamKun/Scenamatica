package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;

@Value
public class TypeDefinition implements IDefinition
{
    ClassNode clazz;
    String name;
    String description;
    TypePropertyDefinition[] properties;
    Type mappingOf;
    Type extending;

    @Override
    public Class<?> getAnnotationType()
    {
        return TypeDoc.class;
    }

    @Override
    public boolean isRelatedTo(@NotNull ClassNode classNode)
    {
        return (this.properties != null && Arrays.stream(this.properties).parallel()
                .anyMatch(property -> property.isRelatedTo(classNode)))
                || (this.mappingOf != null && classNode.name.equals(this.mappingOf.getInternalName()))
                || (this.extending != null && classNode.name.equals(this.extending.getInternalName()));
    }
}
