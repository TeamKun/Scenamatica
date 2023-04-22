package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * コンディションを満たすかチェックできるアクションのインターフェースです。
 */
public interface Requireable<A extends ActionArgument>
{
    /**
     * 条件を満たしているかチェックします。
     *
     * @param argument 引数
     * @param engine   エンジン
     * @return 条件を満たしている場合はtrue
     */
    boolean isConditionFulfilled(@Nullable A argument, @NotNull ScenarioEngine engine);

    /**
     * 対応していない引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     */
    default void throwIfPresent(@NotNull String fieldName, @Nullable Object value)
    {
        if (value != null)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }

    /**
     * 期待値と異なる引数を指定した場合に例外を投げます。
     *
     * @param fieldName フィールド名
     * @param value     値
     * @param expected  期待値
     */
    default void throwIfNotEquals(@NotNull String fieldName, @Nullable Object value, @Nullable Object expected)
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

    default void throwIfTrue(@NotNull String fieldName, boolean value)
    {
        if (value)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }
}
