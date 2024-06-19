package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

@Value
public class TypeDefinition implements IDefinition
{

    @NotNull
    ClassNode clazz;

    String name;

    String description;

    TypePropertyDefinition[] properties;

    Type mappingOf;

    Type extending;

    @Override
    public ClassNode getAnnotatedClass()
    {
        return this.clazz;
    }

    @Override
    public Class<?> getAnnotationType()
    {
        return TypeDoc.class;
    }

    @Override
    public boolean isDependsOn(@NotNull IDefinition def)
    {
        if (!(def instanceof TypeDefinition typeDef))
            return false;

        if (this.properties != null)
            for (TypePropertyDefinition property : this.properties)
                if (property.isDependsOn(typeDef))
                    return true;

        return (this.mappingOf != null && typeDef.name.equals(this.mappingOf.getInternalName()))
                || (this.extending != null && typeDef.name.equals(this.extending.getInternalName()));
    }
}
