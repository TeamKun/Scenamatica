package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TestState;
import org.jetbrains.annotations.NotNull;

/**
 * テスト結果を受け取るインターフェースです。
 * この実装は, {@link java.util.concurrent.CyclicBarrier} を利用しているため, サーバのメインスレッドで実行しないでください。
 */
public interface ScenarioResultDeliverer
{
    /**
     * テスト結果を設定します。
     *
     * @param result テスト結果
     */
    void setResult(TestResult result);

    /**
     * テスト結果を取得します。
     *
     * @param timeout タイムアウト時間(tick)
     *                0以下の場合は無限に待ちます。
     * @param state   テストの状態
     * @return テスト結果
     */
    @NotNull
    TestResult waitResult(long timeout, @NotNull TestState state);

    /**
     * サーバのチックが経過したときに呼び出されます。
     */
    void onTick();

    /**
     * このインスタンスを破棄します。
     */
    void kill();
}
