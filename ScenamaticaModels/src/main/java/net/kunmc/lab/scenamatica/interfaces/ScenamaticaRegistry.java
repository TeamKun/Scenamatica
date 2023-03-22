package net.kunmc.lab.scenamatica.interfaces;

import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;

import java.util.logging.Logger;

/**
 * このプラグインのアプリケーションサービスをまとめるインターフェースです。
 */
public interface ScenamaticaRegistry
{
    /**
     * ロガーを取得します。
     *
     * @return ロガー
     */
    Logger getLogger();

    /**
     * Scenamatica 環境を取得します。
     */
    ScenamaticaEnvironment getEnvironment();

    /**
     * 例外ハンドラーを取得します。
     *
     * @return 例外ハンドラー
     */
    ExceptionHandler getExceptionHandler();

    /**
     * シナリオファイルマネージャーを取得します。
     *
     * @return シナリオファイルマネージャー
     */
    ScenarioFileManager getScenarioFileManager();

    /**
     * コンテキストマネージャーを取得します。
     *
     * @return コンテキストマネージャー
     */
    ContextManager getContextManager();

    /**
     * このデーモンをシャットダウンします。
     */
    void shutdown();
}
