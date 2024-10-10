package org.kunlab.scenamatica.enums;

import lombok.Getter;
import org.yaml.snakeyaml.nodes.Tag;

@Getter
public enum YAMLNodeType
{
    INTEGER(Tag.INT),
    FLOAT(Tag.FLOAT),
    STRING(Tag.STR),
    BOOLEAN(Tag.BOOL),
    NULL(Tag.NULL),
    MAPPING(Tag.MAP),
    LIST(Tag.SEQ),
    BINARY(Tag.BINARY);

    private final Tag tag;

    YAMLNodeType(Tag tag)
    {
        this.tag = tag;
    }

    public static YAMLNodeType fromTag(Tag tag)
    {
        // 新しいタイプは追加されないので, if-else で処理する。
        if (tag.equals(Tag.INT))
            return INTEGER;
        else if (tag.equals(Tag.FLOAT))
            return FLOAT;
        else if (tag.equals(Tag.STR))
            return STRING;
        else if (tag.equals(Tag.BOOL))
            return BOOLEAN;
        else if (tag.equals(Tag.NULL))
            return NULL;
        else if (tag.equals(Tag.MAP))
            return MAPPING;
        else if (tag.equals(Tag.SEQ))
            return LIST;
        else if (tag.equals(Tag.BINARY))
            return BINARY;
        else
            throw new IllegalArgumentException("Unknown tag: " + tag);
    }

    public boolean isTypeOf(Tag tag)
    {
        return this.tag.equals(tag);
    }
}
