package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputsDefinition;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class OutputsDefinitionReader implements IAnnotationReader<OutputsDefinition>
{
    public static final String DESC = Descriptors.getDescriptor(OutputDocs.class);

    private static final String KEY_VALUE = "value";

    private final OutputDefinitionReader outputDefinitionReader;

    public OutputsDefinitionReader(OutputDefinitionReader outputDefinitionReader)
    {
        this.outputDefinitionReader = outputDefinitionReader;
    }

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public OutputsDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        return new OutputsDefinition(
                clazz,
                values.getAsArray(KEY_VALUE, OutputDefinition.class, s -> {
                    assert s instanceof AnnotationNode;
                    AnnotationValues insideValues = AnnotationValues.of((AnnotationNode) s);
                    return this.outputDefinitionReader.buildAnnotation(null, insideValues);
                })
        );
    }
}
