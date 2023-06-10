package net.kunmc.lab.scenamatica.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefinitionsMapper
{
    private static final String KEY_DEFINITIONS = "definitions";
    private static final String KEY_REFERENCE = "$ref";
    private static final Pattern PATTERN_REFERENCE_EMBED = Pattern.compile("\\$\\{([\\w|{}]+)}");
    private static final Map<String, Object> BASE_EMBED_DEFINITIONS;
    private static final Map<Class<?>, Function<String, ?>> CAST_METHODS;

    static
    {
        Map<String, Object> baseEmbedDefinitions = new HashMap<>();

        baseEmbedDefinitions.put("{", "{");  // ${{} で, { をエスケ－プできるように。
        baseEmbedDefinitions.put("}", "}");

        BASE_EMBED_DEFINITIONS = Collections.unmodifiableMap(baseEmbedDefinitions);

        CAST_METHODS = getCastMethods();
    }

    private static Map<Class<?>, Function<String, ?>> getCastMethods()
    {
        Map<Class<?>, Function<String, ?>> castMethods = new HashMap<>();

        // プリミティブ
        castMethods.put(Integer.class, Integer::parseInt);
        castMethods.put(Long.class, Long::parseLong);
        castMethods.put(Double.class, Double::parseDouble);
        castMethods.put(Float.class, Float::parseFloat);
        castMethods.put(Boolean.class, Boolean::parseBoolean);
        castMethods.put(Byte.class, Byte::parseByte);

        // 準プリミティブ
        castMethods.put(String.class, Object::toString);

        // ユーティリティ
        castMethods.put(UUID.class, UUID::fromString);


        return castMethods;
    }

    public static void resolveReferences(Map<String, Object> map)
    {
        if (!map.containsKey(KEY_DEFINITIONS))
            return;

        Map<String, Object> schema = MapUtils.checkAndCastMap(
                map.get(KEY_DEFINITIONS),
                String.class,
                Object.class
        );

        processDefSet(map, schema);
    }

    public static Object resolveReferences(Map<String, Object> map, Map<String, Object> schema)
    {
        return processDefSet(map, schema);
    }

    public static Object resolveReference(Object obj, Map<String, Object> schema)
    {
        return processDefSet(obj, schema);
    }

    private static Object processDefSet(Object obj, Map<String, Object> defs)
    {
        if (obj instanceof Map)
        {
            Map<String, Object> value = MapUtils.checkAndCastMap(obj, String.class, Object.class);
            return processDefsSetOfMap(value, defs);  // なかで再帰
        }
        else if (obj instanceof List)
        {
            // noinspection unchecked
            return processDefsSetOfList((List<Object>) obj, defs);  // なかで再帰
        }

        if (!(obj instanceof String))
            return obj;

        String value = (String) obj;
        return processEmbeddedRef(value, createEmbedDef(defs));   // ${ref} とかの処理
    }

    private static Object processDefsSetOfMap(Map<String, Object> map, Map<String, Object> defs)
    {
        if (map.containsKey(KEY_REFERENCE))
        {
            String ref = (String) map.get(KEY_REFERENCE);
            map.remove(KEY_REFERENCE);
            Object schemaRef = defs.get(ref);

            if (schemaRef instanceof Map)
            {
                // noinspection unchecked
                map.putAll((Map<String, ?>) schemaRef);  // スキーム的に, 型がStringであることは保証されている。はず。
                return map;
            }
            else
                return schemaRef;  // スキーマ取得用に Object にしたが, 実際は String 等の場合。
        }
        else
        {
            for (Map.Entry<String, Object> entry : map.entrySet())
                entry.setValue(processDefSet(entry.getValue(), defs));
            return map;
        }
    }

    private static Object processDefsSetOfList(List<Object> list, Map<String, Object> defs)
    {
        List<Object> result = new ArrayList<>();
        for (Object o : list)
            result.add(processDefSet(o, defs));
        return result;
    }

    private static Object processEmbeddedRef(String value, Map<String, Object> defs)
    {
        Matcher matcher = PATTERN_REFERENCE_EMBED.matcher(value);

        // SnakeYAML が勝手に型推論してしまうので, 文字列として参照を置換した後に, キャストし直す必要があるため。
        Map<Class<?>, Function<String, ?>> casters = new HashMap<>(getCastMethods());

        boolean replaced = false;
        for (int i = 0; matcher.find(i); i = matcher.end(), replaced = true)
        {
            String ref = matcher.group(1);
            String refFull = matcher.group(0);
            Object schemaRef = defs.get(ref);
            if (schemaRef == null)
                continue;

            removeInvalidTypesFromCastMethods(casters, schemaRef.getClass());

            value = value.replace(refFull, schemaRef.toString());
        }

        // 置換されていない場合は、深い置換/型の復元（再変換）は必要ない。
        if (!replaced)
            return value;

        // キャストできる場合は最初の候補でキャストする。
        if (!casters.isEmpty())
            return castTo(value, casters.keySet().iterator().next());

        // 全部の方を網羅しているので, 順に脱落していく。 ここに来るのは String だけ。
        // assert value instanceof String;

        return processEmbeddedRef(value, defs);  // Chu!❤ 再帰関数でごめん
    }

    private static Map<String, Object> createEmbedDef(Map<String, Object> defs)
    {
        Map<String, Object> embedDefs = new HashMap<>(BASE_EMBED_DEFINITIONS);
        embedDefs.putAll(defs);
        return embedDefs;
    }

    private static void removeInvalidTypesFromCastMethods(Map<Class<?>, Function<String, ?>> castMethods, Object obj)
    {
        castMethods.entrySet().removeIf(entry -> !entry.getKey().isInstance(obj));
    }

    private static Object castTo(Object obj, Class<?> clazz)
    {
        Function<String, ?> caster = CAST_METHODS.get(clazz);
        if (caster == null)
            throw new IllegalArgumentException("Unsupported type: " + clazz.getName());

        return caster.apply(obj.toString());
    }
}
