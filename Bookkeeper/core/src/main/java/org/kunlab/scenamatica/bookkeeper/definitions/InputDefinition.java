package org.kunlab.scenamatica.bookkeeper.definitions;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

@Value
public class InputDefinition implements IDefinition
{
    ClassNode clazz;
    String name;
    String description;
    ActionMethod[] requiredOn;
    ActionMethod[] availableFor;
    Type type;
    MCVersion supportsSince;
    MCVersion supportsUntil;
    Object constValue;
    Double max;
    Double min;
    boolean requiresActor;

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
    public boolean isDependsOn(@Nullable ScenamaticaClassLoader classLoader, @NotNull IDefinition def)
    {
        if (this.type == null)
            return false;

        if (def instanceof TypeDefinition)
        {
            TypeDefinition typeDef = (TypeDefinition) def;
            return (typeDef.getName().equals(this.type.getInternalName()))
                    || (typeDef.getMappingOf() != null && typeDef.getMappingOf().getInternalName().equals(this.type.getInternalName()))
                    || (typeDef.getExtending() != null && typeDef.getExtending().getInternalName().equals(this.type.getInternalName()));
        }
        else if (def instanceof InputDefinition)
        {
            InputDefinition inputDef = (InputDefinition) def;
            return this.type.getInternalName().equals(inputDef.getType().getInternalName());
        }

        return false;
    }
}
