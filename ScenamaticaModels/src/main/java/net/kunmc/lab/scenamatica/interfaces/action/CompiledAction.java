package net.kunmc.lab.scenamatica.interfaces.action;

import org.apache.logging.log4j.util.BiConsumer;

import java.util.function.Consumer;

/**
 * アクションをコンパイルした結果を表します。
 *
 * @param <A> アクションの引数の型
 */
public interface CompiledAction<A extends ActionArgument>
{
    /**
     * アクションを取得します。
     *
     * @return アクション
     */
    Action<A> getAction();

    /**
     * アクションの引数を取得します。
     *
     * @return アクションの引数
     */
    A getArgument();

    /**
     * エラーハンドラーを取得します。
     *
     * @return エラーハンドラー
     */
    BiConsumer<CompiledAction<A>, Throwable> getErrorHandler();

    /**
     * アクションが実行されたときに呼び出されるコールバックを取得します。
     *
     * @return コールバック
     */
    Consumer<CompiledAction<A>> getOnExecute();

    /**
     * アクションを実行します。
     */
    void execute();
}
