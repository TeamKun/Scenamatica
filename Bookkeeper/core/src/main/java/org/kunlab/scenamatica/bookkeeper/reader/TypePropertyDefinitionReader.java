package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.bookkeeper.definitions.TypePropertyDefinition;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class TypePropertyDefinitionReader implements IAnnotationReader<TypePropertyDefinition>
{
    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "description";
    public static final String KEY_TYPE = "type";
    public static final String KEY_REQUIRED = "required";
    public static final String KEY_DEFAULT = "defaultValue";
    public static final String KEY_VALUES = "values";
    public static final String KEY_MAPPING = "mappingOf";

    private static final String DESC = Descriptors.getDescriptor(TypeProperty.class);

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public TypePropertyDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        return new TypePropertyDefinition(
                values.getAsString(KEY_NAME),
                values.getAsString(KEY_DESC),
                values.get(KEY_TYPE, Type.class),
                values.getAsBoolean(KEY_REQUIRED),
                values.getAsString(KEY_DEFAULT),
                values.getAsArray(KEY_VALUES, String.class),
                values.get(KEY_MAPPING, Type.class)
        );
    }

}
