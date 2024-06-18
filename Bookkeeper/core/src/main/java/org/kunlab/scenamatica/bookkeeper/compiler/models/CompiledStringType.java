package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CompiledStringType extends CompiledType implements IPrimitiveType
{
    public static final String DESC_UUID = Descriptors.getDescriptor(UUID.class);
    public static final TypeReference REF_UUID = new TypeReference(
            "uuid",
            new CompiledStringType(
                    new CompiledType("uuid", "UUID", UUID.class.getName()), Format.UUID)
    );

    public static final String DESC_NAMESPACED = "Lcom/destroystokyo/paper/Namespaced;"; // これだけのためにクラスパスに入れたくない。
    public static final TypeReference REF_NAMESPACED = new TypeReference(
            "namespaced",
            new CompiledStringType(
                    new CompiledType("namespaced", "Namespaced", "com.destroystokyo.paper.Namespaced"), null,
                    "^([a-z0-9_/]+:)?[a-z0-9_/]+$", null
            )
    );

    private static final String KEY_FORMAT = "format";
    private static final String KEY_PATTERN = "pattern";
    private static final String KEY_ENUMS = "enums";

    private final Format format;
    private final String pattern;
    private final Map<String, String> enums;

    public CompiledStringType(CompiledType original, Format format, String pattern, Map<String, String> enums)
    {
        super(original.getId(), original.getName(), String.class.getName(), original.getMappingOf(), null);
        this.format = format;
        this.pattern = pattern;
        this.enums = enums;
    }

    public CompiledStringType(CompiledType original, Format format)
    {
        this(original, format, null, null);
    }

    public CompiledStringType(CompiledType original)
    {
        this(original, null, null, null);
    }

    public CompiledStringType(CompiledType original, Map<String, String> enums)
    {
        this(original, null, null, enums);
    }

    public CompiledStringType(CompiledType original, List<String> enums)
    {
        this(original, null, null, toMap(enums));
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = super.serialize();
        map.put(KEY_TYPE, "string");
        map.put(KEY_CLASS_NAME, String.class.getName());

        map.put(KEY_FORMAT, this.format == null ? null: this.format.getValue());
        map.put(KEY_PATTERN, this.pattern);
        map.put(KEY_ENUMS, this.enums);

        return map;
    }

    private static Map<String, String> toMap(List<String> enums)
    {
        Map<String, String> map = new HashMap<>();
        for (String e : enums)
            map.put(e, null);

        return map;
    }

    @Getter
    @AllArgsConstructor
    public enum Format
    {
        DATE_TIME("date-time"),
        DATE("date"),
        TIME("time"),
        DURATION("duration"),

        EMAIL("email"),
        IDN_EMAIL("idn-email"),

        HOSTNAME("hostname"),
        IDN_HOSTNAME("idn-hostname"),

        IPV4("ipv4"),
        IPV6("ipv6"),

        URI("uri"),
        URI_REFERENCE("uri-reference"),
        IRI("iri"),
        IRI_REFERENCE("iri-reference"),
        UUID("uuid");

        private final String value;

    }
}
