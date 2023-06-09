package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * シナリオが実行中でないことを表す例外です。
 */
@Getter
public class ScenarioNotRunningException extends ScenarioException
{
    public ScenarioNotRunningException(@NotNull String scenarioName)
    {
        super(scenarioName, "Scenario " + scenarioName + " is not running.");
    }

    public ScenarioNotRunningException()
    {
        super(null, "Scenario is not running.");
    }
}
