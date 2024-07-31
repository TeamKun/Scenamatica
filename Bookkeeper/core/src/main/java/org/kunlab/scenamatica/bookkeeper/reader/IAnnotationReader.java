package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public interface IAnnotationReader<T extends IDefinition>
{
    boolean canRead(@NotNull AnnotationNode anno);

    T buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values);

}
