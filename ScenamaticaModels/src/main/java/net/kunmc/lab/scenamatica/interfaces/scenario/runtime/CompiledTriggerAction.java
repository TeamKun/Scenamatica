package net.kunmc.lab.scenamatica.interfaces.scenario.runtime;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    TriggerBean getTrigger();

    /**
     * 本シナリオの実行前に実行するシナリオを取得します。
     *
     * @return 実行前に実行するシナリオ
     */
    @NotNull
    List<CompiledScenarioAction<?>> getBeforeActions();

    /**
     * 本シナリオの実行後に実行するシナリオを取得します。
     *
     * @return 実行後に実行するシナリオ
     */
    @NotNull
    List<CompiledScenarioAction<?>> getAfterActions();

    /**
     * 本シナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    @Nullable
    CompiledScenarioAction<?> getRunIf();
}
