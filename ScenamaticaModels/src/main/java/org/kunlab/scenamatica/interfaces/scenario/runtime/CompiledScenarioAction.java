package org.kunlab.scenamatica.interfaces.scenario.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;

/**
 * コンパイルされたシナリオアクションを表すインターフェースです。
 *
 * @param <A> アクションの引数
 */
public interface CompiledScenarioAction<A extends ActionArgument>
{
    /**
     * コンパイル前のアクションを取得します。
     *
     * @return コンパイル前のアクション
     */
    @NotNull
    ScenarioBean getBean();

    /**
     * シナリオの種類を取得します。
     *
     * @return シナリオの種類
     */
    @NotNull
    ScenarioType getType();

    /**
     * アクションを取得します。
     *
     * @return アクション
     */
    @NotNull
    CompiledAction<A> getAction();

    /**
     * アクションの実行条件を取得します。
     *
     * @return アクションの実行条件
     */
    @Nullable
    CompiledScenarioAction<?> getRunIf();
}
