package org.kunlab.scenamatica.interfaces.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

/**
 * 動作の引数のインタフェースです。
 */
public interface ActionArgument extends TriggerArgument
{
    /**
     * 引数の文字列を返します。
     *
     * @return 引数の文字列
     */
    String getArgumentString();

    /**
     * 引数が正しいかチェックします。
     *
     * @param engine シナリオエンジン
     * @param type   シナリオの種類
     */
    void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type);
}
