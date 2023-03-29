package net.kunmc.lab.scenamatica.exceptions.scenario;

import lombok.Getter;

/**
 * シナリオが見つからないことを表す例外です。
 */
@Getter
public class ScenarioNotFoundException extends ScenarioException
{
    private final String scenarioName;

    public ScenarioNotFoundException(String scenarioName)
    {
        super("Scenario " + scenarioName + " is not found.");
        this.scenarioName = scenarioName;
    }
}
