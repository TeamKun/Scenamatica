package org.kunlab.scenamatica.action.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractActionArgument implements ActionArgument
{
    protected static String buildArgumentString(Object... kvPairs)
    {
        // Key1, Value1, Key2, Value2, ...

        if (kvPairs.length % 2 != 0)
            throw new IllegalArgumentException("The number of arguments must be even and key-value pairs.");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < kvPairs.length; i += 2)
        {
            Object keyObj = kvPairs[i];
            Object value = kvPairs[i + 1];

            if (value == null)
                continue;  // Exclude nulls

            if (!(keyObj instanceof String))
                throw new IllegalArgumentException("The key must be a string.");

            String key = (String) keyObj;

            if (i != 0)
                builder.append(", ");

            builder.append(key).append("=").append(valueOf(value));
        }

        return builder.toString();
    }

    protected static String appendArgumentString(String base, Object... kvPairs)
    {
        String arg = buildArgumentString(kvPairs);
        if (arg.isEmpty())
            return base;
        else
            return base + ", " + arg;
    }

    private static String valueOf(Object obj)
    {
        if (obj == null)
            return "null";
        else if (obj instanceof String)
            return "\"" + obj + "\"";
        else if (obj instanceof Enum<?>)
            return ((Enum<?>) obj).name();
        else if (obj instanceof Map<?, ?>)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet())
            {
                if (builder.length() > 1)
                    builder.append(", ");
                builder.append(valueOf(entry.getKey())).append("=").append(valueOf(entry.getValue()));
            }

            builder.append("}");
            return builder.toString();
        }
        else if (obj instanceof Collection<?>)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (Object o : (Collection<?>) obj)
            {
                if (builder.length() > 1)
                    builder.append(", ");
                builder.append(valueOf(o));
            }
            builder.append("]");
            return builder.toString();
        }
        else
            return obj.toString();
    }

    /**
     * 対応していない引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     */
    protected static void ensurePresent(@NotNull String fieldName, @Nullable Object value)
    {
        if (value != null)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }

    /**
     * 対応していない引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     */
    protected static void ensurePresent(@NotNull String fieldName, @Nullable Number value)
    {
        if (value != null && value.doubleValue() != -1)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }

    /**
     * 引数が足りない場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     */
    protected static void ensureNotPresent(@NotNull String fieldName, @Nullable Object value)
    {
        if (value == null)
            throw new IllegalArgumentException(String.format("The argument '%s' is required.", fieldName));
    }

    /**
     * 期待値と異なる引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     * @param expected  期待値
     */
    protected static void ensureNotEquals(@NotNull String fieldName, @Nullable Object value, @Nullable Object expected)
    {
        if (!Objects.equals(value, expected))
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }

    /**
     * 期待値と同じ引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     * @param expected  期待値
     */
    protected static void ensureEquals(@NotNull String fieldName, @Nullable Object value, @Nullable Object expected)
    {
        if (Objects.equals(value, expected))
            throw new IllegalArgumentException(String.format("The value of the argument '%s' must not be '%s'.", fieldName, expected));
    }

    /**
     * trueを指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     */

    protected void ensureTrue(@NotNull String fieldName, boolean value)
    {
        if (value)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }

    @Override
    public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
    {
    }
}
