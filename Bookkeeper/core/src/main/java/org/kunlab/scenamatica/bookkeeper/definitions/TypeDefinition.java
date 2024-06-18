package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;

public record TypeDefinition(@NotNull ClassNode clazz, String name, String description,
                             TypePropertyDefinition[] properties,
                             Type mappingOf, Type extending) implements IDefinition
{
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

    @Override
    public String toString()
    {
        return "TypeDefinition{" +
                "clazz=" + this.clazz.name.substring(this.clazz.name.lastIndexOf('/') + 1) +
                ", name='" + this.name + '\'' +
                ", description='" + this.description + '\'' +
                ", properties=" + Arrays.toString(this.properties) +
                ", mappingOf=" + this.mappingOf +
                ", extending=" + this.extending +
                '}';
    }
}
