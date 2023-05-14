package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
