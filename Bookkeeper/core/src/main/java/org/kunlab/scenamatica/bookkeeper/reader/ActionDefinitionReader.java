package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class ActionDefinitionReader implements IAnnotationReader<ActionDefinition>
{
    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "description";
    public static final String KEY_EVENTS = "events";
    public static final String KEY_EXECUTABLE = "executable";
    public static final String KEY_WATCHABLE = "watchable";
    public static final String KEY_REQUIREABLE = "requireable";
    public static final String KEY_SUPPORTS_SINCE = "supportsSince";
    public static final String KEY_SUPPORTS_UNTIL = "supportsUntil";
    public static final String KEY_OUTPUTS = "outputs";

    private static final String DESC = Descriptors.getDescriptor(ActionDoc.class);

    private final OutputDefinitionReader processor;

    public ActionDefinitionReader(OutputDefinitionReader reader)
    {
        this.processor = reader;
    }

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public ActionDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        return new ActionDefinition(
                clazz,
                values.getAsString(KEY_NAME),
                values.getAsString(KEY_DESC),
                values.getAsArray(KEY_EVENTS, Type.class),
                values.getAsString(KEY_EXECUTABLE),
                values.getAsString(KEY_WATCHABLE),
                values.getAsString(KEY_REQUIREABLE),
                values.getAsEnum(KEY_SUPPORTS_SINCE, MCVersion.class),
                values.getAsEnum(KEY_SUPPORTS_UNTIL, MCVersion.class),
                values.getAsArray(KEY_OUTPUTS, OutputDefinition.class, (obj) -> {
                    assert obj instanceof AnnotationNode;
                    AnnotationValues insideValues = AnnotationValues.of((AnnotationNode) obj);

                    return this.processor.buildAnnotation(null, insideValues);
                }),
                null
        );
    }

}
