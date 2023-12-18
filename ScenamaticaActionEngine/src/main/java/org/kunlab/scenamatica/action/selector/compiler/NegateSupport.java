package org.kunlab.scenamatica.action.selector.compiler;

import org.kunlab.scenamatica.action.selector.compiler.parser.NegativeValue;

import java.util.List;
import java.util.Map;

public class NegateSupport
{
    public static boolean isNegative(String key, Map<? super String, Object> properties)
    {
        return properties.containsKey(key) && shouldNegate(key, properties);
    }

    public static boolean isNegative(Object obj)
    {
        return shouldNegate(obj);
    }

    public static Object getRaw(String key, Map<? super String, Object> properties)
    {
        return toRaw(properties.get(key));
    }

    public static boolean shouldNegate(String key, Map<? super String, Object> properties)
    {
        return shouldNegate(properties.get(key));
    }

    public static Object toRaw(Object obj)
    {
        if (obj instanceof NegativeValue)
            return ((NegativeValue) obj).getValue();
        else
            return obj;
    }

    public static <T> T getRawCast(String key, Map<? super String, Object> properties)
    {
        return toRawCast(properties.get(key));
    }

    public static <T> T toRawCast(Object obj)
    {
        // noinspection unchecked
        return (T) toRaw(obj);
    }

    public static List<Object> negateAll(List<Object> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            Object obj = list.get(i);
            if (obj instanceof String)
                list.set(i, new NegativeValue(obj));
        }

        return list;
    }

    public static boolean shouldNegate(Object obj)
    {
        return obj instanceof NegativeValue;
    }

}
