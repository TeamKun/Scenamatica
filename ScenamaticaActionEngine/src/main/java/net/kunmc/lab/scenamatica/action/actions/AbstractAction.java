package net.kunmc.lab.scenamatica.action.actions;

import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractAction<A extends ActionArgument> implements Action<A>
{
    @Override
    public void onStartWatching(@Nullable A argument, @NotNull Plugin plugin, @Nullable Event event)
    {
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable A argument)
    {
    }

    @NotNull
    protected A requireArgsNonNull(@Nullable A argument)
    {
        if (argument == null)
            throw new IllegalArgumentException("Cannot execute action without argument.");

        return argument;
    }

    /**
     * 対応していない引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     */
    protected void throwIfPresent(@NotNull String fieldName, @Nullable Object value)
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
    protected void throwIfPresent(@NotNull String fieldName, @NotNull Number value)
    {
        if (value.doubleValue() != -1)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }

    /**
     * 期待値と異なる引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     * @param expected  期待値
     */
    protected void throwIfNotEquals(@NotNull String fieldName, @Nullable Object value, @Nullable Object expected)
    {
        if (!Objects.equals(value, expected))
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }

    /**
     * trueを指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     */

    protected void throwIfTrue(@NotNull String fieldName, boolean value)
    {
        if (value)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }
}
