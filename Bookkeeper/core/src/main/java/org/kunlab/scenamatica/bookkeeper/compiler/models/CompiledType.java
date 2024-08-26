package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.compiler.ActionCompiler;
import org.kunlab.scenamatica.bookkeeper.compiler.CategoryManager;
import org.kunlab.scenamatica.bookkeeper.compiler.SerializingContext;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.ActionReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.utils.MapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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
    protected static final String KEY_SUPPORTS_SINCE = "supportsSince";
    protected static final String KEY_SUPPORTS_UNTIL = "supportsUntil";

    private final String id;
    private final String name;
    private final String description;
    private final CategoryManager.CategoryEntry category;
    private final String className;
    private final String mappingOf;
    private final Map<String, Property> properties;
    private final GenericAdmonition[] admonitions;
    private final MCVersion supportsSince;
    private final MCVersion supportsUntil;

    public CompiledType(String id, String name, String description, CategoryManager.CategoryEntry category,
                        String className, GenericAdmonition[] admonitions, MCVersion supportsSince, MCVersion supportsUntil)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.className = className;
        this.mappingOf = null;
        this.properties = null;
        this.admonitions = admonitions;
        this.supportsSince = supportsSince;
        this.supportsUntil = supportsUntil;
    }

    public CompiledType(String id, String name, String description, CategoryManager.CategoryEntry category, String className, String mappingOf,
                        MCVersion supportsSince, MCVersion supportsUntil)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.className = className;
        this.mappingOf = mappingOf;
        this.properties = null;
        this.admonitions = null;
        this.supportsSince = supportsSince;
        this.supportsUntil = supportsUntil;
    }

    public CompiledType(String id, String name, String description, CategoryManager.CategoryEntry category,
                        String className, MCVersion supportsSince, MCVersion supportsUntil)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.className = className;
        this.mappingOf = null;
        this.properties = null;
        this.admonitions = null;
        this.supportsSince = supportsSince;
        this.supportsUntil = supportsUntil;
    }

    @Override
    public Map<String, Object> serialize(@NotNull SerializingContext ctxt)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TYPE, "object");
        if (!ctxt.isJSONSchema())
        {
            map.put(KEY_ID, this.id);
            map.put(KEY_NAME, this.name);
            if (this.category != null)
                map.put(KEY_CATEGORY, this.category.getReference());

            MapUtils.putIfNotNull(map, KEY_CLASS_NAME, this.className);
            MapUtils.putIfNotNull(map, KEY_MAPPING_OF, this.mappingOf);
            if (!(this.admonitions == null || this.admonitions.length == 0))
                map.put(KEY_ADMONITIONS, Arrays.stream(this.admonitions).map(GenericAdmonition::serialize).collect(Collectors.toList()));
        }

        MapUtils.putIfNotNull(map, KEY_DESCRIPTION, this.description);
        if (!(this.properties == null || this.properties.isEmpty()))
            map.put(KEY_PROPERTIES, serializeProperties(ctxt));

        MapUtils.putIfNotNull(map, KEY_SUPPORTS_SINCE, this.supportsSince);
        MapUtils.putIfNotNull(map, KEY_SUPPORTS_UNTIL, this.supportsUntil);

        if (ctxt.isJSONSchema() && this.shouldEmbedActionMeta())
            this.embedActionMeta(ctxt, map);
        return map;
    }

    private boolean shouldEmbedActionMeta()
    {
        // ActionStructure はアクション一覧の埋込などする。
        return this.className.equals("org/kunlab/scenamatica/interfaces/structures/scenario/ActionStructure");
    }

    private Map<String, Object> serializeProperties(@NotNull SerializingContext ctxt)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        if (this.properties == null)
            return map;

        for (Map.Entry<String, Property> entry : this.properties.entrySet())
            map.put(entry.getKey(), entry.getValue().serialize(ctxt));

        return map;
    }

    private void embedActionMeta(@NotNull SerializingContext ctxt, @NotNull Map<? super String, Object> serializedActionMap)
    {
        final String ACTION_META_SESSION_DATA_KEY = "CompiledType.ActionMeta";

        if (ctxt.hasSessionData(ACTION_META_SESSION_DATA_KEY))
        {
            String actionMeta = (String) ctxt.getSessionData(ACTION_META_SESSION_DATA_KEY);
            serializedActionMap.put("$ref", actionMeta);
            return;
        }

        List<ActionReference> actions = ctxt.getCore().getCompiler().getCompiler(ActionCompiler.class).getResolvedReferences();

        Map<String, Object> actionsDefinitionMap = new LinkedHashMap<>();
        List<Map<String, Object>> oneOf = new ArrayList<>();
        actionsDefinitionMap.put("oneOf", oneOf);

        for (ActionReference action : actions)
        {
            Map<String, Object> actionMap = new LinkedHashMap<>();
            oneOf.add(actionMap);

            CompiledAction compiledAction = action.getResolved();
            Map<String, CompiledAction.ActionInput> inputs = compiledAction.getInputs();
            actionMap.put("description", compiledAction.getDescription());

            Map<String, Object> properties = new LinkedHashMap<>();
            actionMap.put("properties", properties);

            Map<String, Object> actionSpecifier = new LinkedHashMap<>();
            properties.put("action", actionSpecifier);

            actionSpecifier.put("type", "string");
            actionSpecifier.put("description", "アクションの種類を指定します。");
            actionSpecifier.put("const", compiledAction.getId());

            Map<String, Object> withSpecifier = new LinkedHashMap<>();
            properties.put("with", withSpecifier);

            withSpecifier.put("type", "object");
            withSpecifier.put("description", "アクションに使用する引数を定義します。");
            withSpecifier.put(
                    "properties",
                    inputs.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize(ctxt)))
            );
        }

        String referenceName = ctxt.createReference(serializedActionMap, actionsDefinitionMap);
        ctxt.putSessionData(ACTION_META_SESSION_DATA_KEY, referenceName);
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
        private static final String KEY_SUPPORTS_SINCE = "supportsSince";
        private static final String KEY_SUPPORTS_UNTIL = "supportsUntil";

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
        MCVersion supportsSince;
        MCVersion supportsUntil;

        public Map<String, Object> serialize(@NotNull SerializingContext ctxt)
        {
            Map<String, Object> map = new LinkedHashMap<>();
            if (ctxt.isJSONSchema())
            {
                CompiledType resolvedType = this.type.getResolved();
                Map<String, Object> putMap = map;
                if (this.array)
                {
                    putMap.put("type", "array");
                    putMap.put("items", putMap = new LinkedHashMap<>());
                }
                else if (resolvedType.getClass() == CompiledType.class) // プリミティブを弾く
                {
                    putMap.put("type", "object");
                    Map<String, Object> properties = this.type.getResolved().serializeProperties(ctxt);
                    putMap.put("properties", properties);
                    if (resolvedType.shouldEmbedActionMeta())
                        resolvedType.embedActionMeta(ctxt, map);
                }

                ctxt.createReference(putMap, this.type);

                if (this.array && putMap.get("description") == null)
                    putMap.put("description", this.description);
            }
            else
            {
                map.put(KEY_NAME, this.name);
                map.put(KEY_TYPE, this.type.getReference());
                MapUtils.putIfTrue(map, KEY_ARRAY, this.array);
                if (!(this.admonitions == null || this.admonitions.length == 0))
                    map.put(KEY_ADMONITIONS, Arrays.stream(this.admonitions).map(GenericAdmonition::serialize).collect(Collectors.toList()));
            }

            map.put(KEY_DESCRIPTION, this.description);
            MapUtils.putIfTrue(map, KEY_REQUIRED, this.required);
            MapUtils.putIfNotNull(map, KEY_PATTERN, this.pattern);
            MapUtils.putIfNotNull(map, KEY_MIN, this.min);
            MapUtils.putIfNotNull(map, KEY_MAX, this.max);
            MapUtils.putIfNotNull(map, KEY_DEFAULT_VALUE, this.defaultValue);

            MapUtils.putIfNotNull(map, KEY_SUPPORTS_SINCE, this.supportsSince);
            MapUtils.putIfNotNull(map, KEY_SUPPORTS_UNTIL, this.supportsUntil);

            return map;
        }
    }
}
