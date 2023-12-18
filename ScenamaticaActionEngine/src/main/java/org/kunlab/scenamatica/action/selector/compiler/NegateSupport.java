package org.kunlab.scenamatica.action.selector.compiler;

import org.kunlab.scenamatica.action.selector.compiler.parser.NegativeValue;

import java.util.Map;

public class NegateSupport
{
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

    public static boolean shouldNegate(Object obj)
    {
        return obj instanceof NegativeValue;
    }

}
