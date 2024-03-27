package org.kunlab.scenamatica.interfaces.action;

/**
 * このインターフェースは、アクションの管理を提供します。
 * 初期化、シャットダウン、実行マネージャの取得、コンパイラの取得、ローダの取得などのメソッドが含まれています。
 */
public interface ActionManager
{
    /**
     * アクションマネージャを初期化します。
     */
    void init();

    /**
     * アクションマネージャをシャットダウンします。
     */
    void shutdown();

    /**
     * アクションの実行マネージャを取得します。
     *
     * @return アクションの実行マネージャ。
     */
    ActionRunManager getRunManager();

    /**
     * アクションのコンパイラを取得します。
     *
     * @return アクションのコンパイラ。
     */
    ActionCompiler getCompiler();

    /**
     * アクションのローダを取得します。
     *
     * @return アクションのローダ。
     */
    ActionLoader getLoader();
}
