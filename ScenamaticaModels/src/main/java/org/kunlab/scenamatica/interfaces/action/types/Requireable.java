package org.kunlab.scenamatica.interfaces.action.types;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

/**
 * コンディションのチェックが可能な動作を表すインターフェースです。
 */
public interface Requireable
{
    /**
     * 条件を満たしているかチェックします。
     *
     * @param argument 引数
     * @param engine   エンジン
     * @return 条件を満たしている場合はtrue
     */
    boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine);
}
