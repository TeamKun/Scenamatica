package org.kunlab.scenamatica.interfaces.scenario;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;

/**
 * シナリオの実行結果を受け取るインターフェースです。
 */
public interface ScenarioActionListener
{
    /**
     * アクションが実行されたときに呼び出されます。
     */
    void onActionExecutionFinished(@NotNull ActionResult result);

    /**
     * 監視していたアクションが実行されたときに呼び出されます。
     *
     * @param result   実行結果
     * @param isJumped ジャンプして実行されたかどうか
     */
    void onObservingActionExecuted(@NotNull ActionResult result, boolean isJumped);

    /**
     * 実行を期待するアクションを取得します。
     *
     * @return 実行を期待するアクション
     */
    @Nullable
    CompiledScenarioAction getWaitingFor();

    /**
     * 実行を期待するアクションを設定します。
     *
     * @param waitingFor 実行を期待するアクション
     */
    void setWaitingFor(@Nullable CompiledScenarioAction waitingFor);

    /**
     * アクションの実行に失敗したときに呼び出されます。
     *
     * @param action    実行に失敗したアクション
     * @param throwable 発生した例外
     */
    void onActionError(CompiledAction action, Throwable throwable);
}
