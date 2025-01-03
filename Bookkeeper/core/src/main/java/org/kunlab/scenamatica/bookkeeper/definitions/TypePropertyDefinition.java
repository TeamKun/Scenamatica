package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.bookkeeper.compiler.models.GenericAdmonition;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

@Value
public class TypePropertyDefinition implements IDefinition
{
    ClassNode annotatedClass;
    String name;
    String description;
    Type type;
    boolean required;
    String pattern;
    String defaultValue;
    Double min;
    Double max;
    GenericAdmonition[] admonitions;

    MCVersion supportsSince;
    MCVersion supportsUntil;

    @Override
    public ClassNode getAnnotatedClass()
    {
        return this.annotatedClass;
    }

    @Override
    public Class<?> getAnnotationType()
    {
        return TypeProperty.class;
    }

    @Override
    public boolean isDependsOn(@Nullable ScenamaticaClassLoader classLoader, @NotNull IDefinition classNode)
    {
        if (classNode instanceof TypeDefinition)
        {
            TypeDefinition typeDef = (TypeDefinition) classNode;
            Type thisType = Descriptors.getElementTypeSafe(this.type);
            ClassNode thatClassNode = typeDef.getAnnotatedClass();
            Type thatExtending = Descriptors.getElementTypeSafe(typeDef.getExtending());
            Type thatMappingOf = Descriptors.getElementTypeSafe(typeDef.getMappingOf());


            return (thisType.getInternalName().equals(thatClassNode.name))
                    || (thatExtending != null && thisType.getClassName().equals(thatExtending.getClassName()))
                    || (thatMappingOf != null && thisType.getClassName().equals(thatMappingOf.getClassName()));
        }

        return this.type.getElementType().getClassName().equals(classNode.getAnnotatedClass().name);
    }
}
