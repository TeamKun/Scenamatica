package org.kunlab.scenamatica.bookkeeper.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public class Descriptors
{
    @Contract("null -> null; !null -> !null")
    private static String getTypeDescriptor(Class<?> clazz)
    {
        if (clazz == null)
            return null;

        return "L" + clazz.getName().replace('.', '/') + ";";
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

        String descriptor = null;

        String className = clazz.getName();
        if (clazz.isArray())
            className = clazz.getComponentType().getName();

        if (clazz.isPrimitive() || className.startsWith("java.lang."))
        {
            if (clazz == boolean.class)
                descriptor = "Z";
            if (clazz == byte.class)
                descriptor = "B";
            if (clazz == char.class)
                descriptor = "C";
            if (clazz == short.class)
                descriptor = "S";
            if (clazz == int.class)
                descriptor = "I";
            if (clazz == long.class)
                descriptor = "J";
            if (clazz == float.class)
                descriptor = "F";
            if (clazz == double.class)
                descriptor = "D";
            if (clazz == void.class)
                descriptor = "V";
        }
        else
            descriptor = getTypeDescriptor(clazz);

        if (clazz.isArray())
            return "[" + descriptor;
        else
            return descriptor;
    }

    public static boolean isPrimitive(String className)
    {
        className = className.replace('.', '/');

        boolean isPrimitive = className.equals("boolean") || className.equals("byte") || className.equals("char")
                || className.equals("short") || className.equals("int") || className.equals("long")
                || className.equals("float") || className.equals("double") || className.equals("void");
        if (isPrimitive)
            return true;

        return className.equals("java/lang/Boolean") || className.equals("java/lang/Byte")
                || className.equals("java/lang/Character") || className.equals("java/lang/Short")
                || className.equals("java/lang/Integer") || className.equals("java/lang/Long")
                || className.equals("java/lang/Float") || className.equals("java/lang/Double");
    }

    public static char primitiveToDescriptor(String className)
    {
        switch (className.replace('.', '/'))
        {
            case "boolean":
            case "java/lang/Boolean":
                return 'Z';
            case "byte":
            case "java/lang/Byte":
                return 'B';
            case "char":
            case "java/lang/Character":
                return 'C';
            case "short":
            case "java/lang/Short":
                return 'S';
            case "int":
            case "java/lang/Integer":
                return 'I';
            case "long":
            case "java/lang/Long":
                return 'J';
            case "float":
            case "java/lang/Float":
                return 'F';
            case "double":
            case "java/lang/Double":
                return 'D';
            default:
                throw new IllegalArgumentException("Unknown primitive type: " + className);
        }
    }

    @Contract("null -> null; !null -> !null")
    public static Type getElementTypeSafe(@Nullable Type type)
    {
        if (type == null)
            return null;

        return type.getDescriptor().startsWith("[") ? type.getElementType(): type;
    }
}
