package org.kunlab.scenamatica.nms.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * NMS に対応していない操作が行われた際にスローされる例外です。
 */
public class UnsupportedNMSOperationException extends UnsupportedOperationException
{
    @NotNull
    private final Class<?> className;
    @NotNull
    private final String methodName;
    @NotNull
    private final Class<?> returnType;
    @NotNull
    private final Class<?>[] arguments;

    private UnsupportedNMSOperationException(@NotNull Class<?> className, @NotNull String methodName, @NotNull Class<?> returnType, @NotNull Class<?>... arguments)
    {
        super(buildMessage(className, methodName, returnType, arguments));

        this.className = className;
        this.methodName = methodName;
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public static UnsupportedNMSOperationException of(@NotNull Class<?> className, @NotNull String methodName, @NotNull Class<?> returnType, @NotNull Class<?>... arguments)
    {
        return new UnsupportedNMSOperationException(className, methodName, returnType, arguments);
    }

    public static UnsupportedNMSOperationException ofVoid(@NotNull Class<?> className, @NotNull String methodName, @NotNull Class<?>... arguments)
    {
        return new UnsupportedNMSOperationException(className, methodName, void.class, arguments);
    }

    private static String buildMessage(Class<?> className, String methodName, Class<?> returnType, Class<?>[] arguments)
    {
        return "Unsupported NMS operation: " + buildClassName(className) + "." + buildMethodName(methodName, returnType, arguments);
    }

    private static String buildMethodName(String methodName, Class<?> returnType, Class<?>[] arguments)
    {
        StringBuilder builder = new StringBuilder(methodName);

        builder.append("(");
        for (int i = 0; i < arguments.length; i++)
            builder.append(buildClassName(arguments[i]));
        builder.append(")");

        builder.append(buildClassName(returnType));

        return builder.toString();
    }

    private static String buildClassName(Class<?> clazz)
    {
        StringBuilder builder = new StringBuilder();
        if (clazz.isArray())
            builder.append("[");

        if (clazz.isPrimitive())
        {
            if (clazz == boolean.class)
                builder.append("Z");
            else if (clazz == byte.class)
                builder.append("B");
            else if (clazz == char.class)
                builder.append("C");
            else if (clazz == double.class)
                builder.append("D");
            else if (clazz == float.class)
                builder.append("F");
            else if (clazz == int.class)
                builder.append("I");
            else if (clazz == long.class)
                builder.append("J");
            else if (clazz == short.class)
                builder.append("S");
            else if (clazz == void.class)
                builder.append("V");
        }
        else
        {
            builder.append("L");
            builder.append(clazz.getName().replace(".", "/"));
            builder.append(";");
        }

        return builder.toString();
    }
}
