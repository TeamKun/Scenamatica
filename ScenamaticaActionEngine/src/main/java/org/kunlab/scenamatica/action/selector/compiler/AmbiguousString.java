package org.kunlab.scenamatica.action.selector.compiler;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;

import java.util.Map;
import java.util.regex.Pattern;

@Value
@Nullable
public class AmbiguousString
{
    public static final String KEY_SUFFIX_REGEX = "regex";

    String value;
    Pattern regex;
    boolean doNegate;

    public AmbiguousString(String value, String regex, boolean doNegate)
    {
        this.value = value;
        if (regex == null)
            this.regex = null;
        else
            this.regex = Pattern.compile(regex);
        this.doNegate = doNegate;
    }

    public AmbiguousString(String regex, boolean doNegate)
    {
        this(null, regex, doNegate);
    }

    public static void normalizeMap(String groupKey, String key, Map<? super String, Object> properties)
    {
        if (hasAmbiguousString(key, properties))
        {
            AmbiguousString string = fromMap(key, properties);
            wipeMap(key, properties);

            Map<String, Object> group = MapUtils.createOrRetriveMap(groupKey, properties);
            group.put(key, string);

            return;
        }

        Object parent = properties.get(groupKey);
        if (!(parent instanceof Map))
            return;

        Map<String, Object> parentMap = MapUtils.checkAndCastMap(parent);
        if (parentMap.containsKey(key))
            return;

        AmbiguousString string = fromMap(key, parentMap);
        wipeMap(key, parentMap);
        parentMap.put(key, string);
    }

    public static void wipeMap(String key, Map<? super String, Object> properties)
    {
        properties.remove(key);
        properties.remove(key + "_" + KEY_SUFFIX_REGEX);
    }

    public static boolean hasAmbiguousString(String key, Map<? super String, Object> properties)
    {
        return properties.containsKey(key)
                || properties.containsKey(key + "_" + KEY_SUFFIX_REGEX);
    }

    @NotNull
    public static AmbiguousString fromMap(String key, Map<? super String, Object> properties)
    {
        // 有効な値：
        // 1. 通常の文字列 => value
        // 1. キー_regex => regex
        // 2. Map で { regex: 正規表現 } => regex
        // 3. Map で { <key>: 文字列 } => value
        String regexKey = key + "_" + KEY_SUFFIX_REGEX;
        if (properties.containsKey(regexKey))
        {
            String regex = (String) NegateSupport.toRaw(regexKey);
            return new AmbiguousString(regex, NegateSupport.shouldNegate(regexKey));
        }

        if (!properties.containsKey(key))
        {
            // Normalize されてる場合は到達しない。
            throw new IllegalArgumentException("Invalid number format: " + properties);
        }

        Object value = properties.get(key);
        if (value instanceof AmbiguousString)
            return (AmbiguousString) value;
        else if (value instanceof String)
            return new AmbiguousString(NegateSupport.toRawCast(value), NegateSupport.shouldNegate(value));
        else if (value instanceof Map)
            return parseAmbiguousMapString(key, MapUtils.checkAndCastMap(value));
        else if (value == null)
            return new AmbiguousString(null, false);
        else
            throw new IllegalArgumentException("Invalid string format: " + value);
    }

    private static AmbiguousString parseAmbiguousMapString(String key, Map<? super String, Object> map)
    {
        if (map.containsKey(KEY_SUFFIX_REGEX))
            return new AmbiguousString(
                    NegateSupport.getRawCast(KEY_SUFFIX_REGEX, map),
                    NegateSupport.shouldNegate(KEY_SUFFIX_REGEX, map)
            );

        if (map.containsKey(key))
            return new AmbiguousString(
                    NegateSupport.getRawCast(key, map),
                    NegateSupport.shouldNegate(key, map)
            );

        throw new IllegalArgumentException("Invalid ambiguous string format: " + map);
    }

    public boolean testRaw(String value)
    {
        if (this.regex != null)
            return this.regex.matcher(value).matches();
        else if (this.value != null)
            return this.value.equalsIgnoreCase(value);
        else
            return value == null;
    }

    public boolean test(String value)
    {
        return this.testRaw(value) ^ this.doNegate;
    }
}
