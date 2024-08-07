package org.kunlab.scenamatica.interfaces.scenario.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;

import java.util.List;

/**
 * コンパイルされたトリガアクションを表すインターフェースです。
 */
public interface CompiledTriggerAction
{
    /**
     * トリガを取得します。
     *
     * @return トリガ
     */
    @NotNull
    TriggerStructure getTrigger();

    /**
     * 本シナリオの実行前に実行するシナリオを取得します。
     *
     * @return 実行前に実行するシナリオ
     */
    @NotNull
    List<CompiledScenarioAction> getBeforeActions();

    /**
     * 本シナリオの実行後に実行するシナリオを取得します。
     *
     * @return 実行後に実行するシナリオ
     */
    @NotNull
    List<CompiledScenarioAction> getAfterActions();

    /**
     * 本シナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    @Nullable
    CompiledScenarioAction getRunIf();
}
