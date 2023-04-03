package net.kunmc.lab.scenamatica.interfaces.action;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * コンディションを満たすかチェックできるアクションのインターフェースです。
 */
public interface Requireable<A extends ActionArgument>
{
    /**
     * 条件を満たしているかチェックします。
     *
     * @param argument 引数
     * @param plugin   プラグイン
     * @return 条件を満たしている場合はtrue
     */
    boolean isConditionFulfilled(@Nullable A argument, @NotNull Plugin plugin);

    /**
     * 引数が正しいかチェックします。
     *
     * @param argument 引数
     */
    void validateArgument(@Nullable A argument);

    /**
     * 対応していない引数を指定した場合に例外を投げます。
     */
    default void throwIfPresent(@NotNull String fieldName, @Nullable Object value)
    {
        if (value != null)
            throw new IllegalArgumentException(String.format("The argument '%s' is not supported.", fieldName));
    }
}
