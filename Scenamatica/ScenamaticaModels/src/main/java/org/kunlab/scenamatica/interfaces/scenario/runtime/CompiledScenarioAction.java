package org.kunlab.scenamatica.interfaces.scenario.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;

/**
 * コンパイルされたシナリオアクションを表すインターフェースです。
 */
public interface CompiledScenarioAction
{
    /**
     * コンパイル前のアクションを取得します。
     *
     * @return コンパイル前のアクション
     */
    @NotNull
    ScenarioStructure getStructure();

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
    CompiledAction getAction();

    /**
     * アクションの実行条件を取得します。
     *
     * @return アクションの実行条件
     */
    @Nullable
    CompiledScenarioAction getRunIf();
}
