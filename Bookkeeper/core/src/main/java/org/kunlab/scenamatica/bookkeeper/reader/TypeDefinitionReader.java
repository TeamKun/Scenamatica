package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.definitions.TypeDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.TypePropertyDefinition;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class TypeDefinitionReader implements IAnnotationReader<TypeDefinition>
{
    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "description";
    private static final String KEY_PROPS = "properties";
    private static final String KEY_MAPPING_OF = "mappingOf";
    private static final String KEY_EXTENDING = "extending";

    private static final String DESC = Descriptors.getDescriptor(TypeDoc.class);

    private final TypePropertyDefinitionReader propertyReader;

    public TypeDefinitionReader(TypePropertyDefinitionReader propertyReader)
    {
        this.propertyReader = propertyReader;
    }

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public TypeDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        assert clazz != null;

        return new TypeDefinition(
                clazz,
                values.getAsString(KEY_NAME),
                values.getAsString(KEY_DESC),
                values.getAsArray(KEY_PROPS, TypePropertyDefinition.class, (obj) -> {
                    assert obj instanceof AnnotationNode;
                    AnnotationValues insideValues = AnnotationValues.of((AnnotationNode) obj);

                    return this.propertyReader.buildAnnotation(clazz, insideValues);
                }),
                values.get(KEY_MAPPING_OF, Type.class),
                values.get(KEY_EXTENDING, Type.class)
        );
    }

}
