package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.kunlab.scenamatica.bookkeeper.utils.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CompiledStringType extends CompiledType implements IPrimitiveType
{
    public static final int FORMAT_TYPE = 0;
    public static final int PATTERN_TYPE = 1;
    public static final int ENUMS_TYPE = 2;
    public static final int DEFAULT_TYPE = 3;

    public static final String NAME_UUID = "uuid";
    public static final String DESC_UUID = Descriptors.getDescriptor(UUID.class);
    public static final TypeReference REF_UUID = new TypeReference(
            NAME_UUID,
            new CompiledStringType(
                    new CompiledType(NAME_UUID, "UUID", null, UUID.class.getName()), Format.UUID)
    );

    public static final String NAME_NAMESPACED = "namespaced";
    public static final String DESC_NAMESPACED = "Lcom/destroystokyo/paper/Namespaced;"; // これだけのためにクラスパスに入れたくない。
    public static final TypeReference REF_NAMESPACED = new TypeReference(
            NAME_NAMESPACED,
            new CompiledStringType(
                    new CompiledType(NAME_NAMESPACED, "Namespaced", null, "com.destroystokyo.paper.Namespaced"), null,
                    "^([a-z0-9_/]+:)?[a-z0-9_/]+$", null
            )
    );

    public static final String NAME_NAMESPACED_KEY = "namespacedKey";
    public static final String DESC_NAMESPACED_KEY = "Lorg/bukkit/NamespacedKey;"; // これだけのためにクラスパスに入れたくない。
    public static final TypeReference REF_NAMESPACED_KEY = new TypeReference(
            NAME_NAMESPACED_KEY,
            new CompiledStringType(
                    new CompiledType(NAME_NAMESPACED_KEY, "NamespacedKey", null, "org.bukkit.NamespacedKey"), null,
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
        super(original.getId(), original.getName(), null, String.class.getName(), original.getMappingOf(), null);
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

    public int type()
    {
        if (this.format != null)
            return FORMAT_TYPE;
        else if (this.pattern != null)
            return PATTERN_TYPE;
        else if (this.enums != null)
            return ENUMS_TYPE;
        else
            return DEFAULT_TYPE;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = super.serialize();
        map.put(KEY_TYPE, "string");
        map.put(KEY_CLASS_NAME, String.class.getName().replace('.', '/'));

        MapUtils.putIfNotNull(map, KEY_FORMAT, this.format == null ? null: this.format.getValue());
        MapUtils.putIfNotNull(map, KEY_PATTERN, this.pattern);

        if (this.enums != null)
        {
            boolean isAllValueNull = this.enums.values().stream().allMatch(Objects::isNull);
            if (isAllValueNull)
                map.put(KEY_ENUMS, this.enums.keySet());
            else
                map.put(KEY_ENUMS, this.enums);
        }

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
