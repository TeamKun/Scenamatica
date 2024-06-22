package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.definitions.InputDefinition;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class InputDefinitionReader implements IAnnotationReader<InputDefinition>
{
    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "description";
    public static final String KEY_TYPE = "type";
    public static final String KEY_REQUIRED_ON = "requiredOn";
    public static final String KEY_AVAILABLE_FOR = "availableFor";
    public static final String KEY_CONST_VALUE = "constValue";
    public static final String KEY_MAX = "max";
    public static final String KEY_MIN = "min";
    public static final String KEY_SUPPORTS_SINCE = "supportsSince";
    public static final String KEY_SUPPORTS_UNTIL = "supportsUntil";
    public static final String KEY_REQUIRES_ACTOR = "requiresActor";

    private static final String DESC = Descriptors.getDescriptor(InputDoc.class);

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public InputDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        return new InputDefinition(
                clazz,
                values.getAsString(KEY_NAME),
                values.getAsString(KEY_DESC),
                values.getAsEnumArray(KEY_REQUIRED_ON, ActionMethod.class),
                values.getAsEnumArray(KEY_AVAILABLE_FOR, ActionMethod.class),
                values.get(KEY_TYPE, Type.class),
                values.getAsEnum(KEY_SUPPORTS_SINCE, MCVersion.class),
                values.getAsEnum(KEY_SUPPORTS_UNTIL, MCVersion.class),
                values.get(KEY_CONST_VALUE),
                values.get(KEY_MAX, Double.class),
                values.get(KEY_MIN, Double.class),
                values.getAsBoolean(KEY_REQUIRES_ACTOR)
        );
    }
}
