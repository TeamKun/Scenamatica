package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;

/**
 * シナリオが既に実行中であることを表す例外です。
 */
@Getter
public class ScenarioAlreadyRunningException extends ScenarioException
{
    private final String runningScenarioName;

    public ScenarioAlreadyRunningException(String scenarioName, String runningScenarioName)
    {
        super(scenarioName, "Scenario " + scenarioName + " is already running.");
        this.runningScenarioName = runningScenarioName;
    }

    public ScenarioAlreadyRunningException(String message)
    {
        super(null, message);
        this.runningScenarioName = null;
    }
}
