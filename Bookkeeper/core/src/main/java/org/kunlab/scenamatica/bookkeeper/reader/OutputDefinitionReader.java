package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.compiler.models.GenericAdmonition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class OutputDefinitionReader implements IAnnotationReader<OutputDefinition>
{
    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "description";
    public static final String KEY_TARGET = "target";
    public static final String KEY_TYPE = "type";
    public static final String KEY_SUPPORTS_SINCE = "supportsSince";
    public static final String KEY_SUPPORTS_UNTIL = "supportsUntil";
    public static final String KEY_MIN = "min";
    public static final String KEY_MAX = "max";
    public static final String KEY_ADMONITIONS = "admonitions";

    private static final String DESC = Descriptors.getDescriptor(OutputDoc.class);

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public OutputDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        return new OutputDefinition(
                clazz,
                values.getAsString(KEY_NAME),
                values.getAsString(KEY_DESC),
                values.getAsEnumArray(KEY_TARGET, ActionMethod.class),
                values.get(KEY_TYPE, Type.class),
                values.getAsEnum(KEY_SUPPORTS_SINCE, MCVersion.class),
                values.getAsEnum(KEY_SUPPORTS_UNTIL, MCVersion.class),
                values.get(KEY_MAX, Double.class),
                values.get(KEY_MIN, Double.class),
                GenericAdmonition.byAnnotationValues(values.getAsArray(KEY_ADMONITIONS, AnnotationNode.class))
        );
    }

}
