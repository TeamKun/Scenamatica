package org.kunlab.scenamatica.interfaces.action;

import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * アクションをコンパイルした結果を表します。
 *
 * @param <A> アクションの引数の型
 */
public interface CompiledAction<A extends ActionArgument>
{
    /**
     * アクションの Bean を取得します。
     *
     * @return アクションの Bean
     */
    ActionBean getBean();

    /**
     * アクションの実行を取得します。
     *
     * @return アクションの実行
     */
    Action<A> getExecutor();

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
    BiConsumer<CompiledAction<?>, Throwable> getErrorHandler();

    /**
     * アクションが実行されたときに呼び出されるコールバックを取得します。
     *
     * @return コールバック
     */
    Consumer<CompiledAction<?>> getOnExecute();

    /**
     * アクションを実行します。
     */
    void execute();
}
