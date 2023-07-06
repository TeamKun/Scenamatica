package org.kunlab.scenamatica.action.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.block.AbstractBlockAction;
import org.kunlab.scenamatica.action.actions.player.AbstractPlayerAction;
import org.kunlab.scenamatica.action.actions.scenamatica.AbstractScenamaticaAction;
import org.kunlab.scenamatica.action.actions.server.AbstractServerAction;
import org.kunlab.scenamatica.action.actions.world.AbstractWorldAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractAction<A extends ActionArgument> implements Action<A>
{
    public static List<? extends AbstractAction<?>> getActions()
    {
        List<AbstractAction<?>> actions = new ArrayList<>();

        actions.addAll(AbstractBlockAction.getActions());
        actions.addAll(AbstractPlayerAction.getActions());
        actions.addAll(AbstractServerAction.getActions());
        actions.addAll(AbstractWorldAction.getActions());
        actions.addAll(AbstractScenamaticaAction.getActions());

        return actions;
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
    protected void throwIfPresent(@NotNull String fieldName, @Nullable Number value)
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
    protected void throwIfNotPresent(@NotNull String fieldName, @Nullable Object value)
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
