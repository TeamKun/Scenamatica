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
    EXPECT,
    /**
     * アクションに係るコンディションを確認します。
     */
    REQUIRE,

    /**
     * 特別：未設定です。
     */
    UNSET;

    public static final ActionMethod[] ALL = values();
}
