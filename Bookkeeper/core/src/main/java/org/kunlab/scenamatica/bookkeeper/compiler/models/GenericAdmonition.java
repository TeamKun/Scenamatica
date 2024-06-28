package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.Value;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.kunlab.scenamatica.bookkeeper.utils.MapUtils;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.HashMap;
import java.util.Map;

@Value
public class GenericAdmonition
{
    private static final String DESC_ADMONITION = Descriptors.getDescriptor(Admonition.class);

    private static final String KEY_TYPE = "type";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_ON = "on";

    private static final String KEY_SERIALIZED_TYPE = "value";
    private static final String KEY_SERIALIZED_TITLE = "title";
    private static final String KEY_SERIALIZED_CONTENT = "content";
    private static final String KEY_SERIALIZED_ON = "on";

    AdmonitionType type;
    String title;
    String content;
    ActionMethod[] on;

    public GenericAdmonition(AdmonitionType type, String title, String content, ActionMethod... on)
    {
        this.type = type;
        this.title = title;
        this.content = content;
        this.on = on;
    }

    public Map<String, Object> serialize()
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, this.type.getName());
        MapUtils.putIfNotNull(map, KEY_TITLE, this.title);
        MapUtils.putIfNotNull(map, KEY_CONTENT, this.content);
        for (ActionMethod method : this.on)
            map.put(KEY_ON, method.name());

        return map;
    }

    public static GenericAdmonition byAnnotationValue(AnnotationNode serializedAnnotationValue)
    {
        if (!serializedAnnotationValue.desc.equals(DESC_ADMONITION))
            throw new IllegalArgumentException("AnnotationNode is not an Admonition annotation");

        AnnotationValues annotationValues = AnnotationValues.of(serializedAnnotationValue);

        return new GenericAdmonition(
                AdmonitionType.valueOf(annotationValues.getAsString(KEY_SERIALIZED_TYPE)),
                annotationValues.getAsString(KEY_SERIALIZED_TITLE),
                annotationValues.getAsString(KEY_SERIALIZED_CONTENT),
                annotationValues.getAsEnumArray(KEY_SERIALIZED_ON, ActionMethod.class)
        );
    }

    public static GenericAdmonition[] byAnnotationValues(AnnotationNode[] serializedAnnotationValues)
    {
        if (serializedAnnotationValues == null)
            return new GenericAdmonition[0];

        GenericAdmonition[] admonitions = new GenericAdmonition[serializedAnnotationValues.length];
        for (int i = 0; i < serializedAnnotationValues.length; i++)
            admonitions[i] = byAnnotationValue(serializedAnnotationValues[i]);

        return admonitions;
    }
}
