package org.kunlab.scenamatica.interfaces;

/**
 * 発生した例外を全てキャッチする機能のインターフェースです。
 */
public interface ExceptionHandler
{
    /**
     * 例外をキャッチします。
     *
     * @param e 例外
     */
    void report(Throwable e);
}
