package org.kunlab.scenamatica.bookkeeper.definitions;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public record InputDefinition(ClassNode clazz, String name, String description, ActionMethod[] requiredOn,
                              ActionMethod[] availableFor, Type type, MCVersion supportsSince,
                              MCVersion supportsUntil, Object constValue, double max, double min,
                              boolean requiresActor) implements IDefinition
{
    @Override
    public ClassNode getAnnotatedClass()
    {
        return this.clazz;
    }

    @Override
    public Class<?> getAnnotationType()
    {
        return InputDoc.class;
    }

    @Override
    public boolean isDependsOn(@NotNull IDefinition def)
    {
        if (this.type == null)
            return false;

        if (def instanceof TypeDefinition typeDef)
            return (typeDef.name().equals(this.type.getInternalName()))
                    || (typeDef.mappingOf() != null && typeDef.mappingOf().getInternalName().equals(this.type.getInternalName()))
                    || (typeDef.extending() != null && typeDef.extending().getInternalName().equals(this.type.getInternalName()));
        else if (def instanceof InputDefinition inputDef)
            return this.type.getInternalName().equals(inputDef.type().getInternalName());

        return false;
    }
}
