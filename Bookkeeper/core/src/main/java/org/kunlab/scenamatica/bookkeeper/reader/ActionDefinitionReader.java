package org.kunlab.scenamatica.bookkeeper.reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.compiler.models.GenericAdmonition;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.InputDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.utils.ClassAnalyser;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.List;

public class ActionDefinitionReader implements IAnnotationReader<ActionDefinition>
{
    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "description";
    public static final String KEY_EVENTS = "events";
    public static final String KEY_EXECUTABLE = "executable";
    public static final String KEY_EXPECTABLE = "expectable";
    public static final String KEY_REQUIREABLE = "requireable";
    public static final String KEY_SUPPORTS_SINCE = "supportsSince";
    public static final String KEY_SUPPORTS_UNTIL = "supportsUntil";
    public static final String KEY_OUTPUTS = "outputs";
    public static final String KEY_ADMONITIONS = "admonitions";
    public static final String KEY_ACTION_KIND_OF = "actionKindOf";
    private static final String DESC = Descriptors.getDescriptor(ActionDoc.class);
    private static final String ACTION_DESC = "Lorg/kunlab/scenamatica/annotations/action/Action;";

    private final ScenamaticaClassLoader classLoader;
    private final InputDefinitionReader inputReader;
    private final OutputDefinitionReader outputReader;

    public ActionDefinitionReader(ScenamaticaClassLoader classLoader, InputDefinitionReader inputReader, OutputDefinitionReader outputReader)
    {
        this.classLoader = classLoader;
        this.inputReader = inputReader;
        this.outputReader = outputReader;
    }

    @Override
    public boolean canRead(@NotNull AnnotationNode anno)
    {
        return anno.desc.equals(DESC);
    }

    @Override
    public ActionDefinition buildAnnotation(@Nullable ClassNode clazz, @NotNull AnnotationValues values)
    {
        assert clazz != null;

        InputDefinition[] inputs = this.getInputs(clazz);
        return new ActionDefinition(
                clazz,
                scrapeActionID(clazz),
                values.getAsString(KEY_NAME),
                values.getAsString(KEY_DESC),
                values.getAsArray(KEY_EVENTS, Type.class),
                values.getAsString(KEY_EXECUTABLE),
                values.getAsString(KEY_EXPECTABLE),
                values.getAsString(KEY_REQUIREABLE),
                values.getAsEnum(KEY_SUPPORTS_SINCE, MCVersion.class),
                values.getAsEnum(KEY_SUPPORTS_UNTIL, MCVersion.class),
                inputs,
                values.getAsArray(KEY_OUTPUTS, OutputDefinition.class, (obj) -> {
                    assert obj instanceof AnnotationNode;
                    AnnotationValues insideValues = AnnotationValues.of((AnnotationNode) obj);

                    return this.outputReader.buildAnnotation(null, insideValues);
                }),
                GenericAdmonition.byAnnotationValues(values.getAsArray(KEY_ADMONITIONS, AnnotationNode.class)),
                values.getAsEnum(KEY_ACTION_KIND_OF, ActionMethod.class)
        );
    }

    private InputDefinition[] getInputs(ClassNode clazz)
    {
        List<InputDefinition> inputs = new ArrayList<>();
        List<ClassNode> supers = ClassAnalyser.getSuperClasses(this.classLoader, clazz);
        List<FieldNode> fields = new ArrayList<>(clazz.fields);
        supers.stream()
                .map((superClass) -> superClass.fields)
                .forEach(fields::addAll);


        for (FieldNode field : fields)
        {
            if ((field.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) != 0 && (field.access & Opcodes.ACC_FINAL) == 0)
                continue;

            List<AnnotationNode> annos = field.invisibleAnnotations;
            if (annos == null)
                continue;

            for (AnnotationNode anno : annos)
            {
                if (!this.inputReader.canRead(anno))
                    continue;

                AnnotationValues values = AnnotationValues.of(anno);
                InputDefinition input = this.inputReader.buildAnnotation(clazz, values);
                inputs.add(input);
            }
        }

        return inputs.toArray(new InputDefinition[0]);
    }

    private static String scrapeActionID(ClassNode clazz)
    {
        List<AnnotationNode> annos = clazz.visibleAnnotations;
        if (annos == null)
            throw new IllegalStateException("Action annotation not found in " + clazz.name);

        for (AnnotationNode anno : annos)
        {
            if (!anno.desc.equals(ACTION_DESC))
                continue;

            AnnotationValues values = AnnotationValues.of(anno);
            return values.getAsString("value");
        }

        throw new IllegalStateException("Action annotation not found in " + clazz.name);
    }
}
