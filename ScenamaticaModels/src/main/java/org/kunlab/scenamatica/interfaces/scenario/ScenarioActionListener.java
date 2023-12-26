package org.kunlab.scenamatica.interfaces.scenario;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;

/**
 * シナリオの実行結果を受け取るインターフェースです。
 */
public interface ScenarioActionListener
{
    /**
     * アクションの実行にエラーが発生したときに呼び出されます。
     *
     * @param action エラーが発生したアクション
     * @param error  エラー
     */
    void onActionError(@NotNull CompiledAction action, @NotNull Throwable error);

    /**
     * アクションが実行されたときに呼び出されます。
     *
     * @param action 実行されたアクション
     */
    void onActionExecuted(@NotNull CompiledAction action);

    /**
     * 監視対象のアクションがプラグインによって実行されたときに呼び出されます。
     *
     * @param entry 監視対象のアクション
     * @param event 発生したイベント
     * @param <A>   アクションの引数
     */
    void onActionFired(@NotNull WatchingEntry entry, @NotNull Event event);

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
}
