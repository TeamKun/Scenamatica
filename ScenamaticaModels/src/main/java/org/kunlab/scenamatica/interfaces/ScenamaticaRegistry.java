package org.kunlab.scenamatica.interfaces;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionManager;
import org.kunlab.scenamatica.interfaces.context.ContextManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioManager;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import org.kunlab.scenamatica.interfaces.trigger.TriggerManager;

import java.util.logging.Logger;

/**
 * このプラグインのアプリケーションサービスをまとめるインターフェースです。
 * <p>
 * これの使用前に, {@link net.kunmc.lab.peyangpaperutils.lang.LangProvider} を初期化してください。
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

    /**
     * シナリオマネージャーを取得します。
     *
     * @return シナリオマネージャー
     */
    ScenarioManager getScenarioManager();

    /**
     * テストレポーターを返します。
     *
     * @return テストレポーター
     */
    @NotNull
    TestReporter getTestReporter();
}
