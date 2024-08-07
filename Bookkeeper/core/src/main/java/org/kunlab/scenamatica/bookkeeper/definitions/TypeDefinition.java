package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.compiler.models.GenericAdmonition;
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
    GenericAdmonition[] admonitions;

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
    public boolean isDependsOn(@Nullable ScenamaticaClassLoader classLoader, @NotNull IDefinition def)
    {
        if (!(def instanceof TypeDefinition))
            return false;

        TypeDefinition typeDef = (TypeDefinition) def;

        if (this.properties != null)
            for (TypePropertyDefinition property : this.properties)
                if (property.isDependsOn(null, typeDef))
                    return true;

        return (this.mappingOf != null && typeDef.name.equals(this.mappingOf.getInternalName()))
                || (this.extending != null && typeDef.name.equals(this.extending.getInternalName()));
    }
}
