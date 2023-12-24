package org.kunlab.scenamatica.interfaces.action;

import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * アクションをコンパイルした結果を表します。
 */
public interface CompiledAction
{
    /**
     * アクションに関連付けられた Engine を取得します。
     *
     * @return Engine
     */
    ScenarioEngine getEngine();

    /**
     * アクションの Structure を取得します。
     *
     * @return アクションの Structure
     */
    ActionStructure getStructure();

    /**
     * アクションの実行を取得します。
     *
     * @return アクションの実行
     */
    Action getExecutor();

    /**
     * アクションの引数を取得します。
     *
     * @return アクションの引数
     */
    InputBoard getArgument();

    /**
     * エラーハンドラーを取得します。
     *
     * @return エラーハンドラー
     */
    BiConsumer<CompiledAction, Throwable> getErrorHandler();

    /**
     * アクションが実行されたときに呼び出されるコールバックを取得します。
     *
     * @return コールバック
     */
    Consumer<CompiledAction> getOnExecute();
}
