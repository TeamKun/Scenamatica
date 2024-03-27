package org.kunlab.scenamatica.interfaces.action.types;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionContext;

/**
 * コンディションのチェックが可能な動作を表すインターフェースです。
 */
public interface Requireable
{
    /**
     * 条件を満たしているかチェックします。
     *
     * @param ctxt 動作の実行コンテキスト
     * @return 条件を満たしている場合は true
     */
    boolean checkConditionFulfilled(@NotNull ActionContext ctxt);
}
