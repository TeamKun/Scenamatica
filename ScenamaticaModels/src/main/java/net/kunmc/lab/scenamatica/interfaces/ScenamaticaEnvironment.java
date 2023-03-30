package net.kunmc.lab.scenamatica.interfaces;

import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * Scenamatica の実行環境を表すインターフェースです。
 */
public interface ScenamaticaEnvironment
{
    /**
     * Scenamatica デーモンを使用するプラグインを取得します。
     *
     * @return プラグイン
     */
    Plugin getPlugin();

    /**
     * ロガーを取得します。
     *
     * @return ロガー
     */
    Logger getLogger();

    /**
     * 例外ハンドラを取得します。
     *
     * @return 例外ハンドラ
     */
    ExceptionHandler getExceptionHandler();

    /**
     * テストレポーターを返します。
     *
     * @return テストレポーター
     */
    @NotNull
    TestReporter getTestReporter();
}
