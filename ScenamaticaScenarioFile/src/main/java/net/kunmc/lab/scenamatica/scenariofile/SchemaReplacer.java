package net.kunmc.lab.scenamatica.scenariofile;

import net.kunmc.lab.scenamatica.commons.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaReplacer
{
    private static final String SCHEMA_KEY = "schema";
    private static final String SCHEMA_REF = "$ref";
    private static final Pattern SCHEMA_REF_EMBED = Pattern.compile("\\$\\{\\w+}");

    public static void resolveSchemaMap(Map<String, Object> map)
    {
        if (!map.containsKey(SCHEMA_KEY))
            return;

        Map<String, Object> schema = MapUtils.checkAndCastMap(
                map.get(SCHEMA_KEY),
                String.class,
                Object.class
        );

        processSchemaSet(map, schema);
    }

    public static Object resolveSchemaMap(Map<String, Object> map, Map<String, Object> schema)
    {
        return processSchemaSet(map, schema);
    }

    private static Object processSchemaSet(Object obj, Map<String, Object> schema)
    {
        if (obj instanceof Map)
        {
            Map<String, Object> value = MapUtils.checkAndCastMap(obj, String.class, Object.class);
            if (value.containsKey(SCHEMA_REF))
            {
                String ref = (String) value.get(SCHEMA_REF);
                value.remove(SCHEMA_REF);
                Object schemaRef = schema.get(ref);

                if (schemaRef instanceof Map)
                {
                    // noinspection unchecked
                    value.putAll((Map<String, ?>) schemaRef);  // スキーム的に, 型がStringであることは保証されている。はず。
                    return value;
                }
                else
                    return schemaRef; // スキーマ取得用に Object にしたが, 実際は String 等の場合。
            }
            else
            {
                for (Map.Entry<String, Object> entry : value.entrySet())
                    entry.setValue(processSchemaSet(entry.getValue(), schema));
                return value;
            }
        }
        else if (obj instanceof List)
        {
            List<Object> result = new ArrayList<>();
            List<?> value = (List<?>) obj;
            for (Object o : value)
                result.add(processSchemaSet(o, schema));  // chu! 再帰関数でごめん♥
            return result;
        }

        if (!(obj instanceof String))
            return obj;

        String value = (String) obj;
        return processSchemaRef(value, schema);   // ${ref} とかの処理
    }

    private static Object processSchemaRef(String value, Map<String, Object> schema)
    {
        Matcher matcher = SCHEMA_REF_EMBED.matcher(value);

        boolean isIntOrLong = true;  // こいつらは SnakeYAML の型推論対策用。
        boolean isDouble = true;
        boolean isFloat = true;
        boolean isBoolean = true;
        boolean looped = false;
        for (int i = 0; matcher.find(i); i = matcher.end(), looped = true)
        {
            String ref = matcher.group();
            Object schemaRef = schema.get(ref);
            if (schemaRef == null)
                throw new IllegalArgumentException("Schema reference not found: " + ref);

            if (!(schemaRef instanceof Integer || schemaRef instanceof Long))
                isIntOrLong = false;
            if (!(schemaRef instanceof Double))
                isDouble = false;
            if (!(schemaRef instanceof Float))
                isFloat = false;
            if (!(schemaRef instanceof Boolean))
                isBoolean = false;

            value = value.replace(ref, schemaRef.toString());
        }

        if (!looped)
            return value;

        if (isIntOrLong)
            return Long.parseLong(value);
        if (isDouble)
            return Double.parseDouble(value);
        if (isFloat)
            return Float.parseFloat(value);
        if (isBoolean)
            return Boolean.parseBoolean(value);

        // 脱落形式なので, ここに来るのは String だけ。
        return value;
    }

}
