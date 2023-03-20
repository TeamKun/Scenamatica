package net.kunmc.lab.scenamatica.interfaces;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;

/**
 * このプラグインのアプリケーションサービスをまとめるインターフェースです。
 */
public interface ScenamaticaRegistry
{
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
}
