package org.kunlab.scenamatica.interfaces.action.types;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

public interface Executable
{
    /**
     * 動作を実行します。
     *
     * @param engine   シナリオエンジン
     * @param argument 動作の引数
     */
    void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument);
}
