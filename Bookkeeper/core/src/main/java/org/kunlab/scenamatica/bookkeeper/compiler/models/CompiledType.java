package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class CompiledType implements ICompiled
{
    protected static final String KEY_TYPE = "type";
    protected static final String KEY_ID = "id";
    protected static final String KEY_NAME = "name";
    protected static final String KEY_CLASS_NAME = "class";
    protected static final String KEY_MAPPING_OF = "mapping_of";
    protected static final String KEY_PROPERTIES = "properties";

    private final String id;
    private final String name;
    private final String className;
    private final String mappingOf;
    private final Map<String, Property> properties;

    public CompiledType(String id, String name, String className, String mappingOf)
    {
        this.id = id;
        this.name = name;
        this.className = className;
        this.mappingOf = mappingOf;
        this.properties = null;
    }

    public CompiledType(String id, String name, String className)
    {
        this.id = id;
        this.name = name;
        this.className = className;
        this.mappingOf = null;
        this.properties = null;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, "object");
        map.put(KEY_ID, this.id);
        map.put(KEY_NAME, this.name);
        map.put(KEY_CLASS_NAME, this.className);
        map.put(KEY_MAPPING_OF, this.mappingOf);
        map.put(KEY_PROPERTIES, serializeProperties());

        return map;
    }

    private Map<String, Object> serializeProperties()
    {
        Map<String, Object> map = new HashMap<>();
        if (this.properties == null)
            return map;

        for (Map.Entry<String, Property> entry : this.properties.entrySet())
            map.put(entry.getKey(), entry.getValue().serialize());
        return map;
    }

    @Value
    public static class Property
    {
        private static final String KEY_NAME = "name";
        private static final String KEY_TYPE = "type";
        private static final String KEY_DESCRIPTION = "description";
        private static final String KEY_ARRAY = "array";
        private static final String KEY_REQUIRED = "required";
        private static final String KEY_DEFAULT_VALUE = "default";

        String name;
        TypeReference type;
        String description;
        boolean required;
        boolean array;
        Object defaultValue;

        public Property(String name, TypeReference type, String description, boolean required, boolean array,
                        Object defaultValue)
        {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
            this.array = array;
            this.defaultValue = defaultValue;
        }

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_NAME, this.name);
            map.put(KEY_TYPE, this.type.getID());
            map.put(KEY_DESCRIPTION, this.description);
            map.put(KEY_ARRAY, this.array);
            map.put(KEY_REQUIRED, this.required);
            map.put(KEY_DEFAULT_VALUE, this.defaultValue);
            return map;
        }
    }
}
