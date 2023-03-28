package net.kunmc.lab.scenamatica.interfaces;

import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import net.kunmc.lab.scenamatica.interfaces.trigger.TriggerManager;
import org.bukkit.plugin.Plugin;

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
     * Scenamatica デーモンを利用するプラグインを取得します。
     *
     * @return プラグイン
     */
    Plugin getPlugin();

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
     * 動作マネージャーを取得します。
     *
     * @return 動作マネージャー
     */
    ActionManager getActionManager();

    /**
     * トリガマネージャーを取得します。
     *
     * @return トリガマネージャー
     */
    TriggerManager getTriggerManager();

    void init();

    /**
     * このデーモンをシャットダウンします。
     */
    void shutdown();

    net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioManager getScenarioManager();
}
