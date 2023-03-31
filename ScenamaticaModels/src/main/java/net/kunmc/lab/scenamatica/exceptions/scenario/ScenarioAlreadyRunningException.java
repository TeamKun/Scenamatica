package net.kunmc.lab.scenamatica.exceptions.scenario;

import lombok.Getter;

/**
 * シナリオが既に実行中であることを表す例外です。
 */
@Getter
public class ScenarioAlreadyRunningException extends ScenarioException
{
    private final String scenarioName;
    private final String runningScenarioName;

    public ScenarioAlreadyRunningException(String scenarioName, String runningScenarioName)
    {
        super("Scenario " + scenarioName + " is already running.");
        this.scenarioName = scenarioName;
        this.runningScenarioName = runningScenarioName;
    }

    public ScenarioAlreadyRunningException(String message)
    {
        super(message);
        this.scenarioName = null;
        this.runningScenarioName = null;
    }
}
