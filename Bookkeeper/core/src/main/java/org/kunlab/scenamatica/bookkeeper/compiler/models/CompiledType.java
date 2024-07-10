package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.kunlab.scenamatica.bookkeeper.compiler.CategoryManager;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.utils.MapUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CompiledType implements ICompiled
{
    protected static final String KEY_TYPE = "type";
    protected static final String KEY_ID = "id";
    protected static final String KEY_DESCRIPTION = "description";
    protected static final String KEY_CATEGORY = "category";
    protected static final String KEY_NAME = "name";
    protected static final String KEY_CLASS_NAME = "class";
    protected static final String KEY_MAPPING_OF = "mapping_of";
    protected static final String KEY_PROPERTIES = "properties";
    protected static final String KEY_ADMONITIONS = "admonitions";

    private final String id;
    private final String name;
    private final String description;
    private final CategoryManager.CategoryEntry category;
    private final String className;
    private final String mappingOf;
    private final Map<String, Property> properties;
    private final GenericAdmonition[] admonitions;

    public CompiledType(String id, String name, String description, CategoryManager.CategoryEntry category, String className, GenericAdmonition[] admonitions)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.className = className;
        this.mappingOf = null;
        this.properties = null;
        this.admonitions = admonitions;
    }

    public CompiledType(String id, String name, String description, CategoryManager.CategoryEntry category, String className, String mappingOf)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.className = className;
        this.mappingOf = mappingOf;
        this.properties = null;
        this.admonitions = null;
    }

    public CompiledType(String id, String name, String description, CategoryManager.CategoryEntry category, String className)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.className = className;
        this.mappingOf = null;
        this.properties = null;
        this.admonitions = null;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TYPE, "object");
        map.put(KEY_ID, this.id);
        map.put(KEY_NAME, this.name);
        map.put(KEY_DESCRIPTION, this.description);
        if (this.category != null)
            map.put(KEY_CATEGORY, this.category.getReference());
        MapUtils.putIfNotNull(map, KEY_CLASS_NAME, this.className);
        MapUtils.putIfNotNull(map, KEY_MAPPING_OF, this.mappingOf);
        if (!(this.properties == null || this.properties.isEmpty()))
            map.put(KEY_PROPERTIES, serializeProperties());
        if (!(this.admonitions == null || this.admonitions.length == 0))
            map.put(KEY_ADMONITIONS, Arrays.stream(this.admonitions).map(GenericAdmonition::serialize).collect(Collectors.toList()));

        return map;
    }

    private Map<String, Object> serializeProperties()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        if (this.properties == null)
            return map;

        for (Map.Entry<String, Property> entry : this.properties.entrySet())
            map.put(entry.getKey(), entry.getValue().serialize());
        return map;
    }

    @Value
    public static class Property
    {
        public static final String KEY_PATTERN = "pattern";
        public static final String KEY_MIN = "min";
        public static final String KEY_MAX = "max";
        private static final String KEY_NAME = "name";
        private static final String KEY_TYPE = "type";
        private static final String KEY_DESCRIPTION = "description";
        private static final String KEY_ARRAY = "array";
        private static final String KEY_REQUIRED = "required";
        private static final String KEY_DEFAULT_VALUE = "default";
        private static final String KEY_ADMONITIONS = "admonitions";

        String name;
        TypeReference type;
        String description;
        boolean required;
        boolean array;
        String pattern;
        Double min;
        Double max;
        Object defaultValue;
        GenericAdmonition[] admonitions;

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(KEY_NAME, this.name);
            map.put(KEY_TYPE, this.type.getReference());
            map.put(KEY_DESCRIPTION, this.description);
            MapUtils.putIfTrue(map, KEY_ARRAY, this.array);
            MapUtils.putIfTrue(map, KEY_REQUIRED, this.required);
            MapUtils.putIfNotNull(map, KEY_PATTERN, this.pattern);
            MapUtils.putIfNotNull(map, KEY_MIN, this.min);
            MapUtils.putIfNotNull(map, KEY_MAX, this.max);
            MapUtils.putIfNotNull(map, KEY_DEFAULT_VALUE, this.defaultValue);
            if (!(this.admonitions == null || this.admonitions.length == 0))
                map.put(KEY_ADMONITIONS, Arrays.stream(this.admonitions).map(GenericAdmonition::serialize).collect(Collectors.toList()));
            return map;
        }
    }
}
