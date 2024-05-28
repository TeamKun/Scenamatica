package org.kunlab.scenamatica.bookkeeper.enums;

/**
 * アクションの実行方法を定義します。
 */
public enum ActionMethod
{
    /**
     * アクションを実行します。
     */
    EXECUTE,
    /**
     * アクションが実行されるかを監視します。
     */
    WATCH,
    /**
     * アクションに係るコンディションを確認します。
     */
    REQUIRE;

    public static final ActionMethod[] ALL = values();
}
