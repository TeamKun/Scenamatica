package org.kunlab.scenamatica.action.selector.compiler;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;

@Value
@Nullable
public class RangedNumber
{
    public static final String KEY_MIN_SUFFIX = "min";
    public static final String KEY_MAX_SUFFIX = "max";
    public static final String VALUE_SEPARATOR = "..".replace(".", "\\.");

    Number min;
    Number max;

    public static void normalizeMap(String groupKey, String key, Map<? super String, Object> properties)
    {
        if (hasRangedNumber(key, properties))
        {
            RangedNumber number = fromMap(key, properties);
            wipeMap(key, properties);

            Map<String, Object> group = MapUtils.createOrRetriveMap(groupKey, properties);
            group.put(key, number);

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

        RangedNumber number = fromMap(key, parentMap);
        wipeMap(key, parentMap);
        parentMap.put(key, number);
    }

    public static boolean hasRangedNumber(String key, Map<? super String, Object> properties)
    {
        return properties.containsKey(key)
                || properties.containsKey(key + "_" + KEY_MIN_SUFFIX)
                || properties.containsKey(key + "_" + KEY_MAX_SUFFIX);
    }

    public static void wipeMap(String key, Map<? super String, Object> properties)
    {
        properties.remove(key);
        properties.remove(key + "_" + KEY_MIN_SUFFIX);
        properties.remove(key + "_" + KEY_MAX_SUFFIX);
    }

    @NotNull
    public static RangedNumber fromMap(String key, Map<? super String, Object> properties)
    {
        // 有効な値：
        // 1. 単一の Number
        // 2. String で "min~max" || "min~" || "~max" の形式
        // 3. Map で {min: 1, max: 2} の形式

        Number min = null;
        Number max = null;

        if (properties.containsKey(key + "_" + KEY_MIN_SUFFIX))
            min = (Number) properties.get(key + "_" + KEY_MIN_SUFFIX);
        if (properties.containsKey(key + "_" + KEY_MAX_SUFFIX))
            max = (Number) properties.get(key + "_" + KEY_MAX_SUFFIX);

        if (!(min == null && max == null))
            return new RangedNumber(min, max);

        if (properties.containsKey(key))
        {
            Object value = properties.get(key);
            if (value instanceof RangedNumber)
                return (RangedNumber) value;
            else if (value instanceof Number)
                return new RangedNumber((Number) value, (Number) value);
            else if (value instanceof String)
                return parseRangedStringNumber((String) value);
            else if (value instanceof Map)
                return parseRangedMapNumber(MapUtils.checkAndCastMap(value));
            else if (value == null)
                return new RangedNumber(null, null);
            else
                throw new IllegalArgumentException("Invalid number format: " + value);
        }

        // Normalize されてる場合は到達しない。
        throw new IllegalArgumentException("Invalid number format: " + properties);
    }

    private static RangedNumber parseRangedMapNumber(Map<String, Object> map)
    {
        Number min = null;
        Number max = null;

        if (map.containsKey(KEY_MIN_SUFFIX))
            min = (Number) map.get(KEY_MIN_SUFFIX);
        if (map.containsKey(KEY_MAX_SUFFIX))
            max = (Number) map.get(KEY_MAX_SUFFIX);

        if (!(min == null && max == null))
            return new RangedNumber(min, max);
        else
            throw new IllegalArgumentException("Invalid ranged number format: " + map);
    }

    private static RangedNumber parseRangedStringNumber(String value)
    {
        try
        {
            if (!value.contains(VALUE_SEPARATOR))
            {
                Number number = NumberFormat.getInstance().parse(value);
                return new RangedNumber(number, number);
            }

            String[] values = value.split(VALUE_SEPARATOR);
            Number min = null;
            Number max = null;

            if (!values[0].isEmpty())  // ~hoge は, ["", "hoge"] になる
                min = NumberFormat.getInstance().parse(values[0]);
            if (values.length > 1 && !values[1].isEmpty())
                max = NumberFormat.getInstance().parse(values[1]);

            return new RangedNumber(min, max);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Invalid number format: " + value);
        }
    }

    public boolean test(@Nullable Number number)
    {
        return (number != null || this.min == null && this.max == null)
                && (this.min == null || this.min.doubleValue() <= number.doubleValue())
                && (this.max == null || this.max.doubleValue() >= number.doubleValue());
    }
}
