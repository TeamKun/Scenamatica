package net.kunmc.lab.scenamatica.interfaces.action;

import org.apache.logging.log4j.util.BiConsumer;

import java.util.function.Consumer;

/**
 * アクションのキューに入れるためのエントリーです。
 *
 * @param <A> アクションの引数の型
 */
public interface ActionQueueEntry<A extends ActionArgument>
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
    BiConsumer<ActionQueueEntry<A>, Throwable> getErrorHandler();

    /**
     * アクションが実行されたときに呼び出されるコールバックを取得します。
     *
     * @return コールバック
     */
    Consumer<ActionQueueEntry<A>> getOnExecute();

    /**
     * アクションを実行します。
     */
    void execute();
}
