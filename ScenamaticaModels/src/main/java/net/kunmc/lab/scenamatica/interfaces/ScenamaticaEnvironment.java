package net.kunmc.lab.scenamatica.interfaces;

import java.util.logging.Logger;

/**
 * Scenamatica の実行環境を表すインターフェースです。
 */
public interface ScenamaticaEnvironment
{
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

}
