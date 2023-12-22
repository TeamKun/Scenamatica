package org.kunlab.scenamatica.selector.compiler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Value
@Nullable
public class AmbiguousString
{
    public static final String KEY_SUFFIX_REGEX = "regex";

    String value;
    Pattern regex;
    @Getter(AccessLevel.NONE)
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

    public static void normalizeMap(String key, Map<? super String, Object> properties)
    {
        normalizeMap(key, key, properties);
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
        else if (groupKey.equals(key))
            return;

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

    public static void normalizeList(List<Object> list)
    {
        list.replaceAll(AmbiguousString::fromObject);
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
        return fromObject(value);
    }

    public static AmbiguousString fromObject(Object object)
    {
        return fromObject(null, object);
    }

    public static AmbiguousString fromObject(String key, Object object)
    {
        Object rawObject = NegateSupport.toRaw(object);
        if (rawObject instanceof AmbiguousString)
            return (AmbiguousString) rawObject;
        else if (rawObject instanceof Map)
            return parseAmbiguousMapString(key, MapUtils.checkAndCastMap(rawObject));
        else if (rawObject == null)
            return new AmbiguousString(null, false);
        else
            return new AmbiguousString(rawObject.toString(), NegateSupport.shouldNegate(rawObject));
    }

    private static AmbiguousString parseAmbiguousMapString(String key, Map<? super String, Object> map)
    {
        if (map.containsKey(KEY_SUFFIX_REGEX))
            return new AmbiguousString(
                    NegateSupport.getRawCast(KEY_SUFFIX_REGEX, map),
                    NegateSupport.shouldNegate(KEY_SUFFIX_REGEX, map)
            );

        if (key != null && map.containsKey(key))
            return new AmbiguousString(
                    NegateSupport.getRawCast(key, map),
                    NegateSupport.shouldNegate(key, map)
            );

        throw new IllegalArgumentException("Invalid ambiguous string format: " + map);
    }

    public boolean doNegate()
    {
        return this.doNegate;
    }

    public boolean isEmpty()
    {
        return (this.value == null || this.value.isEmpty())
                && this.regex == null;
    }

    public boolean testRaw(String value)
    {
        if (this.regex != null)
            return this.regex.matcher(value).matches();
        else if (this.value != null)
            return this.value.equalsIgnoreCase(value);
        else
            return value == null || value.isEmpty();
    }

    public boolean test(String value)
    {
        return this.testRaw(value) ^ this.doNegate;
    }
}
