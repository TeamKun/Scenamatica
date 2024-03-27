package org.kunlab.scenamatica.interfaces.action.types;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionContext;

public interface Executable
{
    /**
     * 動作を実行します。
     *
     * @param ctxt 動作の実行コンテキスト
     */
    void execute(@NotNull ActionContext ctxt);
}
