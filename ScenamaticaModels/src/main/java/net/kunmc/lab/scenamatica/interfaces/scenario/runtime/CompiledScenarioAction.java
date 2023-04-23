package net.kunmc.lab.scenamatica.interfaces.scenario.runtime;

import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
