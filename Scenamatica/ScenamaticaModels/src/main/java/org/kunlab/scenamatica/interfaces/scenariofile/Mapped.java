package org.kunlab.scenamatica.interfaces.scenariofile;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Bukkit のクラスなどと互換性があることを表すインターフェースです。
 */
public interface Mapped<T>
{
    /**
     * この構造を既存のオブジェクトに適応します。
     *
     * @param object 適応するオブジェクト
     */
    void applyTo(@NotNull T object);

    /**
     * この構造が, ターゲットに対して十分満たしているかどうかを返します。
     *
     * @param object   オブジェクト
     * @param isStrict 厳密なチェックを行うかどうか
     * @return 満たしている場合は true
     */
    @Contract("null, _ -> false")
    boolean isAdequate(@Nullable T object, boolean isStrict);

    /**
     * この構造が, ターゲットに対して十分満たしているかどうかを返します。
     * ゆるいチェックを行います。
     *
     * @param object オブジェクト
     * @return 満たしている場合は true
     */
    default boolean isAdequate(@NotNull T object)
    {
        return this.isAdequate(object, false);
    }

    /**
     * この構造が, ターゲットに適応可能かどうかを返します。
     * @param target ターゲット
     * @return 適応可能な場合は true
     */
    boolean canApplyTo(@Nullable Object target);
}
