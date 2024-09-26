package org.kunlab.scenamatica.interfaces.scenario;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.action.ActionResult;

/**
 * テスト結果を受け取るインターフェースです。
 * この実装は, {@link java.util.concurrent.CyclicBarrier} を利用しているため, サーバのメインスレッドで実行しないでください。
 */
public interface ActionResultDeliverer
{
    /**
     * テスト結果を設定します。
     *
     * @param result テスト結果
     */
    void setResult(ActionResult result);

    /**
     * テスト結果を取得します。
     *
     * @param timeout タイムアウト時間(tick)
     *                0以下の場合は無限に待ちます。
     * @param state   テストの状態
     * @return テスト結果
     */
    @NotNull
    ActionResult waitForResult(long timeout, @NotNull ScenarioState state);

    /**
     * テスト結果を待っているかどうかを取得します。
     *
     * @return テスト結果を待っているかどうか
     */
    boolean isWaiting();

    /**
     * サーバのチックが経過したときに呼び出されます。
     */
    void onTick();

    /**
     * シナリオの待機をタイムアウトさせます。
     */
    void timedout();

    /**
     * このインスタンスで発生した例外を取得します。
     *
     * @param caughtException 例外
     */
    void setCaughtException(Throwable caughtException);

    /**
     * このインスタンスを破棄します。
     */
    void kill();
}
