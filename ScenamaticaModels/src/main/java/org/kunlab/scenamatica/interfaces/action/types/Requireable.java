package org.kunlab.scenamatica.interfaces.action.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

/**
 * コンディションを満たすかチェックできるアクションのインターフェースです。
 */
public interface Requireable<A extends ActionArgument>
{
    /**
     * 条件を満たしているかチェックします。
     *
     * @param argument 引数
     * @param engine   エンジン
     * @return 条件を満たしている場合はtrue
     */
    boolean isConditionFulfilled(@Nullable A argument, @NotNull ScenarioEngine engine);
}
