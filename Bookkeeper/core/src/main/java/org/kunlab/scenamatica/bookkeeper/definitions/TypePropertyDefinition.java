package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public record TypePropertyDefinition(ClassNode annotatedClass, String name, String description, Type type,
                                     boolean required, String pattern, String defaultValue, double min,
                                     double max) implements IDefinition
{
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
    public boolean isDependsOn(@NotNull IDefinition classNode)
    {
        if (classNode instanceof TypeDefinition typeDef)
        {
            Type thisType = Descriptors.getElementTypeSafe(this.type);
            ClassNode thatClassNode = typeDef.getAnnotatedClass();
            Type thatExtending = Descriptors.getElementTypeSafe(typeDef.extending());
            Type thatMappingOf = Descriptors.getElementTypeSafe(typeDef.mappingOf());


            return (thisType.getInternalName().equals(thatClassNode.name))
                    || (thatExtending != null && thisType.getClassName().equals(thatExtending.getClassName()))
                    || (thatMappingOf != null && thisType.getClassName().equals(thatMappingOf.getClassName()));
        }

        return this.type.getElementType().getClassName().equals(classNode.getAnnotatedClass().name);
    }
}
