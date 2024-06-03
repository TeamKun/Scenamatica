package org.kunlab.scenamatica.bookkeeper.utils;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Method;

public class Descriptors
{
    @Contract("null -> null; !null -> !null")
    private static String getTypeDescriptor(Class<?> clazz)
    {
        if (clazz == null)
            return null;

        StringBuilder sb = new StringBuilder("L");
        sb.append(clazz.getName().replace('.', '/'));

        return sb.append(";").toString();
    }

    public static String getDescriptor(Class<?> clazz, Method method)
    {
        StringBuilder sb = new StringBuilder();
        if (clazz != null)
            sb.append(getDescriptor(clazz));

        sb.append(method.getName());
        sb.append("(");

        for (Class<?> param : method.getParameterTypes())
            sb.append(getDescriptor(param));

        sb.append(")").append(getDescriptor(method.getReturnType()));

        return sb.toString();
    }

    public static String getDescriptor(Class<?> clazz)
    {
        if (clazz == null)
            return null;

        if (clazz.isPrimitive())
        {
            if (clazz == boolean.class)
                return "Z";
            if (clazz == byte.class)
                return "B";
            if (clazz == char.class)
                return "C";
            if (clazz == short.class)
                return "S";
            if (clazz == int.class)
                return "I";
            if (clazz == long.class)
                return "J";
            if (clazz == float.class)
                return "F";
            if (clazz == double.class)
                return "D";
            if (clazz == void.class)
                return "V";
        }
        else if (clazz.isArray())
            return "[" + getTypeDescriptor(clazz.getComponentType());
        else
            return getTypeDescriptor(clazz);

        return null;
    }
}
