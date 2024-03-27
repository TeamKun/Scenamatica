package org.kunlab.scenamatica.interfaces.action;

import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.function.BiConsumer;

/**
 * アクションをコンパイルした結果を表します。
 */
public interface CompiledAction
{
    /**
     * アクションに関連付けられた Context
     *
     * @return Engine
     */
    ActionContext getContext();

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
    BiConsumer<ActionResult, ScenarioType> getOnExecute();
}
