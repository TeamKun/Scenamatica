package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionCategoryDefinition;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class ActionCategoryDefinitionReader implements IAnnotationReader<ActionCategoryDefinition>
{
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_INHERIT = "inherit";
    private static final String DESC = Descriptors.getDescriptor(ActionCategoryDefinition.class);

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public ActionCategoryDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        return new ActionCategoryDefinition(
                clazz,
                values.getAsString(KEY_ID),
                values.getAsString(KEY_NAME),
                values.getAsString(KEY_DESCRIPTION),
                values.getAsBoolean(KEY_INHERIT)
        );
    }

}
