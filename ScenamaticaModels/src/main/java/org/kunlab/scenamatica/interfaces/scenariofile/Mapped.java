package org.kunlab.scenamatica.interfaces.scenariofile;

/**
 * Bukkit のクラスなどと互換性があることを表すインターフェースです。
 */
public interface Mapped<T>
{
    /**
     * この構造を既存のオブジェクトに適応します。
     *
     * @param object 適応先のオブジェクト
     */
    void applyTo(T object);

    /**
     * この構造が既存のオブジェクトに十分かどうかを比較します。
     *
     * @param object 比較するオブジェクト
     * @param strict 厳密に比較するかどうか
     * @return 十分かどうか
     */
    boolean isAdequate(T object, boolean strict);

    /**
     * この構造が既存のオブジェクトに十分かどうかを比較します。
     *
     * @param object 比較するオブジェクト
     * @return 十分かどうか
     */
    default boolean isAdequate(T object)
    {
        return this.isAdequate(object, false);
    }
}
