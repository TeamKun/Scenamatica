package org.kunlab.scenamatica.interfaces.scenariofile;

/**
 * Bukkit のクラスなどと互換性があることを表すインターフェースです。
 */
public interface Mapped
{
    /* <!-- Implicitly defined methods -->
    // この構造を既存のオブジェクトに適応します。
    void applyTo(Object object);
    // 既存のオブジェクトがこの構造を満たしているかどうかを確認します。
    boolean isAdequate(Object object);

     */
    boolean canApplyTo(Object target);
}
